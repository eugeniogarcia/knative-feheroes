# Acceso a servicios externos

## Sidecar
Todas las llamadas a servicios externos pasan por el sidecar. Comprobamos cual es la configuracion en el sidecar respecto a llamadas a servicios externos:

```
./kubectl get configmap istio -n istio-system -o yaml | grep -o "mode: ALLOW_ANY"

mode: ALLOW_ANY
mode: ALLOW_ANY
```

Vemos que la configuracion es la por defecto, es decir, que se hace un passthrough. Sino fuera asi, hacemos:  

```
./kubectl get configmap istio -n istio-system -o yaml | sed 's/mode: REGISTRY_ONLY/mode: ALLOW_ANY/g' | ./kubectl replace -n istio-system -f -
```

Ahora nos conectamos a un pod:  

```
./kubectl exec -it heroes-v2-7b74bfd99f-xbglp -c heroes -- bash
```

Llamamos a un servicio externo:

```
curl http://httpbin.org/headers

{
  "headers": {
    "Accept": "*/*",
    "Host": "httpbin.org",
    "User-Agent": "curl/7.52.1",
    "X-B3-Sampled": "1",
    "X-B3-Spanid": "474743d7fec2b390",
    "X-B3-Traceid": "bd738576933fa9df474743d7fec2b390",
    "X-Envoy-Expected-Rq-Timeout-Ms": "15000",
    "X-Istio-Attributes": "Cj8KCnNvdXJjZS51aWQSMRIva3ViZXJuZXRlczovL2hlcm9lcy12Mi03Yjc0YmZkOTlmLTVjZnY0LmRlZmF1bHQ="
  }
}
```

__Todo funciona__. Vamos a restringir ahora el passthrough, de modo que solo podamos llamar a servicios externos que esten previamente registrados:  
```
./kubectl get configmap istio -n istio-system -o yaml | sed 's/mode: ALLOW_ANY/mode: REGISTRY_ONLY/g' | ./kubectl replace -n istio-system -f -

configmap/istio replaced
```

Comprobemos ahora la llamada al mismo servicio externo 

Ahora nos conectamos a un pod:  

```
./kubectl exec -it heroes-v2-7b74bfd99f-xbglp -c heroes -- bash

curl -v http://httpbin.org/headers

*   Trying 3.223.234.9...
* TCP_NODELAY set
* Connected to httpbin.org (3.223.234.9) port 80 (#0)
> GET /headers HTTP/1.1
> Host: httpbin.org
> User-Agent: curl/7.52.1
> Accept: */*
>
< HTTP/1.1 502 Bad Gateway
< location: http://httpbin.org/headers
< date: Sat, 07 Sep 2019 14:28:33 GMT
< server: envoy
< content-length: 0
<
* Curl_http_done: called premature == 0
* Connection #0 to host httpbin.org left intact
```

Añadimos este servicio externo al registro:  

```
./kubectl apply -f - <<EOF
apiVersion: networking.istio.io/v1alpha3
kind: ServiceEntry
metadata:
  name: httpbin-ext
spec:
  hosts:
  - httpbin.org
  ports:
  - number: 80
    name: http
    protocol: HTTP
  resolution: DNS
  location: MESH_EXTERNAL
EOF
```

Repetimos la llamada:

```
curl http://httpbin.org/headers

{
  "headers": {
    "Accept": "*/*",
    "Host": "httpbin.org",
    "User-Agent": "curl/7.52.1",
    "X-B3-Sampled": "1",
    "X-B3-Spanid": "a057c2092e015c1f",
    "X-B3-Traceid": "e8603142633725fda057c2092e015c1f",
    "X-Envoy-Decorator-Operation": "httpbin.org:80/*",
    "X-Istio-Attributes": "CikKGGRlc3RpbmF0aW9uLnNlcnZpY2UuaG9zdBINEgtodHRwYmluLm9yZwoqCh1kZXN0aW5hdGlvbi5zZXJ2aWNlLm5hbWVzcGFjZRIJEgdkZWZhdWx0CikKGGRlc3RpbmF0aW9uLnNlcnZpY2UubmFtZRINEgtodHRwYmluLm9yZwo/Cgpzb3VyY2UudWlkEjESL2t1YmVybmV0ZXM6Ly9oZXJvZXMtdjItN2I3NGJmZDk5Zi01Y2Z2NC5kZWZhdWx0"
  }
}
```

Podemos ver en el log del sidecar la llamada:  

```
./kubectl logs heroes-v2-7b74bfd99f-xbglp -c istio-proxy | tail
```
2019-09-07T14:20:28.138735Z     info    Starting proxy agent
2019-09-07T14:20:28.138967Z     info    watching /etc/certs for changes
2019-09-07T14:20:28.138985Z     info    Received new config, resetting budget
2019-09-07T14:20:28.138989Z     info    Reconciling retry (budget 10)
2019-09-07T14:20:28.138996Z     info    Epoch 0 starting
```

Destacar esta entrada:

```
2019-09-07T14:20:28.138985Z     info    Received new config, resetting budget
```

Podemos definir un virtualservice sobre este servicio externo. En este ejemplo estamos fijando el timeout en tres segundos:  

```
./kubectl apply -f - <<EOF
apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  name: httpbin-ext
spec:
  hosts:
    - httpbin.org
  http:
  - timeout: 3s
    route:
      - destination:
          host: httpbin.org
        weight: 100
EOF
```

Vamos a probarlo. Llamemos forzando un retardo de 5 segs:  

```
curl http://httpbin.org/delay/5

upstream request timeoutroot@heroes-v2-7b74bfd99f-5cfv4
```

Ahora con 2 segundos:  

```
curl http://httpbin.org/delay/2

{
  "args": {},
  "data": "",
  "files": {},
  "form": {},
  "headers": {
    "Accept": "*/*",
    "Host": "httpbin.org",
    "User-Agent": "curl/7.52.1",
    "X-B3-Sampled": "1",
    "X-B3-Spanid": "97a34956c5a9d701",
    "X-B3-Traceid": "733e084be4924df297a34956c5a9d701",
    "X-Envoy-Decorator-Operation": "httpbin.org:80/*",
    "X-Envoy-Expected-Rq-Timeout-Ms": "3000",
    "X-Istio-Attributes": "CikKGGRlc3RpbmF0aW9uLnNlcnZpY2UuaG9zdBINEgtodHRwYmluLm9yZwoqCh1kZXN0aW5hdGlvbi5zZXJ2aWNlLm5hbWVzcGFjZRIJEgdkZWZhdWx0CikKGGRlc3RpbmF0aW9uLnNlcnZpY2UubmFtZRINEgtodHRwYmluLm9yZwo/Cgpzb3VyY2UudWlkEjESL2t1YmVybmV0ZXM6Ly9oZXJvZXMtdjItN2I3NGJmZDk5Zi01Y2Z2NC5kZWZhdWx0"
```

## Saltar el sidecar
Podemos saltarnos el sidecar en las llamadas externas especificando el rango de IPs que deben ser excluidas del sidecar.