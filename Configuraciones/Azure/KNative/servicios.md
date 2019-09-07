# Servicios
## Routes
Se crean automaticamente una serie de servicios. Lo de tipo `ExternalName` se crean asociados al recurso `route`, y los `ClusterIP` se crearon al crear la `configuration` - junto con el `deployment`, `replication set` y los `pods`.  

Por ejemplo, aqui tenemos dos rutas:  
```
apiVersion: serving.knative.dev/v1alpha1
kind: Route
metadata:
  name: heroes-svc
  namespace: heroes-kn-ns
spec:
  traffic:
  - revisionName: heroes-svc-s7cv5
    name: v2
    percent: 70
  - revisionName: heroes-svc-xsrk4
    name: v1
    percent: 30
---
apiVersion: serving.knative.dev/v1alpha1
kind: Route
metadata:
  name: heroes-last-svc
  namespace: heroes-kn-ns
spec:
  traffic:
  - configurationName: heroes-svc
    percent: 100
```  
 
Las rutas son:  
```
heroes-svc
v1-heroes-svc
v2-heroes-svc
```
y
```
heroes-last-svc
```
Si vemos los servicios podemos ver estas rutas definidas como servicios de tipo `ExternalName`:  

```
S [EUGENIO] >kubectl get svc -n heroes-kn-ns

NAME                       TYPE           CLUSTER-IP     EXTERNAL-IP                                           PORT(S)             AGE
feheroes-svc               ExternalName   <none>         istio-ingressgateway.istio-system.svc.cluster.local   <none>              16h
feheroes-svc-ppx8r         ClusterIP      10.0.112.190   <none>                                                80/TCP              16h
feheroes-svc-ppx8r-thmkr   ClusterIP      10.0.20.190    <none>                                                9090/TCP,9091/TCP   16h
feheroes-svc-ppx8r-vxgh6   ClusterIP      10.0.248.168   <none>                                                80/TCP              16h
heroes-last-svc            ExternalName   <none>         istio-ingressgateway.istio-system.svc.cluster.local   <none>              16h
heroes-svc                 ExternalName   <none>         istio-ingressgateway.istio-system.svc.cluster.local   <none>              16h
heroes-svc-s7cv5           ClusterIP      10.0.38.192    <none>                                                80/TCP              16h
heroes-svc-s7cv5-8lnz6     ClusterIP      10.0.105.186   <none>                                                9090/TCP,9091/TCP   16h
heroes-svc-s7cv5-fb6sm     ClusterIP      10.0.192.66    <none>                                                80/TCP              16h
heroes-svc-xsrk4           ClusterIP      10.0.208.128   <none>                                                80/TCP              16h
heroes-svc-xsrk4-8mvlz     ClusterIP      10.0.194.236   <none>                                                80/TCP              16h
heroes-svc-xsrk4-dt9nz     ClusterIP      10.0.169.163   <none>                                                9090/TCP,9091/TCP   16h
v1-heroes-svc              ExternalName   <none>         istio-ingressgateway.istio-system.svc.cluster.local   <none>              16h
v2-heroes-svc              ExternalName   <none>         istio-ingressgateway.istio-system.svc.cluster.local   <none>              16h
```

El external service que se usa para las rutas corresponde con el ingress service de istio:  

```
PS [EUGENIO] >kubectl get svc istio-ingressgateway -n istio-system

NAME                   TYPE           CLUSTER-IP   EXTERNAL-IP      PORT(S)                                                                                                                                      AGE
istio-ingressgateway   LoadBalancer   10.0.31.18   104.40.176.161   15020:31063/TCP,80:31380/TCP,443:31390/TCP,31400:31400/TCP,15029:32387/TCP,15030:30309/TCP,15031:30660/TCP,15032:30560/TCP,15443:32026/TCP   4d17h
```
### Gateways
Hay dos gateways creados como parte de la instalacion de `Kantive-serving`: 

```
PS [EUGENIO] >kubectl get gateways --all-namespaces

NAMESPACE         NAME                      AGE
knative-serving   cluster-local-gateway     2d
knative-serving   knative-ingress-gateway   2d
```
### Virtualservices
Cada ruta además de tener el servicio externo asociado, tiene tambien un virtual service, o mejor dicho, dos. Uno esta asociado al gateway `mesh`, y el otro a los dos gateways que acabamos de ver. El gateway `mesh` es un gateway reservado por el que entra todo el trafico generado desde los `sideacars`. Esto es, cuando las peticiones viene desde el exterior del cluster se procesaran por los dos gateways que he listado arriba, y cuando la peticion viene desde el interior del cluster, desde los sidecars, llegaran por el gateway `mesh`.  

```
PS [EUGENIO] >kubectl get virtualservices --all-namespaces

NAMESPACE      NAME                   GATEWAYS                                                                       HOSTS
heroes-kn-ns   feheroes-svc           [knative-serving/cluster-local-gateway knative-serving/knative-ingress-gateway]   [feheroes-svc.heroes-kn-ns feheroes-svc.heroes-kn-ns.example.com feheroes-svc.heroes-kn-ns.svc feheroes-svc.heroes-kn-ns.svc.cluster.local cd8a294ec7ac5d0ad57a66226c305ff4.probe.invalid]

heroes-kn-ns   feheroes-svc-mesh      [mesh]                                                                            [feheroes-svc.heroes-kn-ns.svc.cluster.local 2254b028bc360fedfb565e44a6ce7c42.probe.invalid]

heroes-kn-ns   heroes-last-svc        [knative-serving/cluster-local-gateway knative-serving/knative-ingress-gateway]   [heroes-last-svc.heroes-kn-ns heroes-last-svc.heroes-kn-ns.example.com heroes-last-svc.heroes-kn-ns.svc heroes-last-svc.heroes-kn-ns.svc.cluster.local 3823d06018f208f44c6745952ae2654c.probe.invalid]

heroes-kn-ns   heroes-last-svc-mesh   [mesh]                                                                            [heroes-last-svc.heroes-kn-ns.svc.cluster.local 7aae897b97b432f01fbc83d61463642f.probe.invalid]

heroes-kn-ns   heroes-svc             [knative-serving/cluster-local-gateway knative-serving/knative-ingress-gateway]   [heroes-svc.heroes-kn-ns heroes-svc.heroes-kn-ns.example.com heroes-svc.heroes-kn-ns.svc heroes-svc.heroes-kn-ns.svc.cluster.local v1-heroes-svc.heroes-kn-ns v1-heroes-svc.heroes-kn-ns.example.com v1-heroes-svc.heroes-kn-ns.svc v1-heroes-svc.heroes-kn-ns.svc.cluster.local v2-heroes-svc.heroes-kn-ns v2-heroes-svc.heroes-kn-ns.example.com v2-heroes-svc.heroes-kn-ns.svc v2-heroes-svc.heroes-kn-ns.svc.cluster.local 73538e32757118ab85a5dba85926c10c.probe.invalid]

heroes-kn-ns   heroes-svc-mesh        [mesh]                                                                            [heroes-svc.heroes-kn-ns.svc.cluster.local v1-heroes-svc.heroes-kn-ns.svc.cluster.local v2-heroes-svc.heroes-kn-ns.svc.cluster.local 594d18eea6863817fd83a52d4216e004.probe.invalid]
```

Si vemos la definicion del servicio virtual, por ejemplo del mesh, veremos que:  
- Se verifica que el gatewat sea mesh, tanto en el `spec` como en cada una de los `match`  
- La authority tiene como opcional el dominio y el puerto, de modo que si hacemos `http://heroes-svc.heroes-kn-ns/` se cumplira la regla, y el virtual service enviara la peticion a una ruta. en el otro virtualservice que se crea, el que esta asociado a los dos gateways, se exige que el authority incluya el dominio.  

```
PS [EUGENIO] >kubectl get virtualservice heroes-svc-mesh -n heroes-kn-ns -o yaml     

apiVersion: networking.istio.io/v1alpha3
kind: VirtualService
metadata:
  annotations:
    kubectl.kubernetes.io/last-applied-configuration: |
{"apiVersion":"serving.knative.dev/v1alpha1","kind":"Route","metadata":{"annotations":{},"name":"heroes-svc","namespace":"heroes-kn-ns"},"spec":{"traffic":[{"name":"v2","percent":70,"revisionName":"heroes-svc-s7cv5"},{"name":"v1","percent":30,"revisionName":"heroes-svc-xsrk4"}]}}
    networking.knative.dev/ingress.class: istio.ingress.networking.knative.dev
  labels:
    serving.knative.dev/route: heroes-svc
    serving.knative.dev/routeNamespace: heroes-kn-ns
  name: heroes-svc-mesh
  namespace: heroes-kn-ns
spec:
  gateways:
  - mesh
  hosts:
  - heroes-svc.heroes-kn-ns.svc.cluster.local
  - v1-heroes-svc.heroes-kn-ns.svc.cluster.local
  - v2-heroes-svc.heroes-kn-ns.svc.cluster.local
  - 594d18eea6863817fd83a52d4216e004.probe.invalid
  http:
  - match:
    - authority:
        regex: ^heroes-svc\.heroes-kn-ns(\.svc(\.cluster\.local)?)?(?::\d{1,5})?$
      gateways:
      - mesh
    retries:
      attempts: 3
      perTryTimeout: 10m0s
    route:
    - destination:
        host: heroes-svc-s7cv5.heroes-kn-ns.svc.cluster.local
        port:
          number: 80
      headers:
        request:
          add:
            Knative-Serving-Namespace: heroes-kn-ns
            Knative-Serving-Revision: heroes-svc-s7cv5
      weight: 70
    - destination:
        host: heroes-svc-xsrk4.heroes-kn-ns.svc.cluster.local
        port:
          number: 80
      headers:
        request:
          add:
            Knative-Serving-Namespace: heroes-kn-ns
            Knative-Serving-Revision: heroes-svc-xsrk4
      weight: 30
    timeout: 10m0s
    websocketUpgrade: true
  - match:
    - authority:
        regex: ^v1-heroes-svc\.heroes-kn-ns(\.svc(\.cluster\.local)?)?(?::\d{1,5})?$
      gateways:
      - mesh
    retries:
      attempts: 3
      perTryTimeout: 10m0s
    route:
    - destination:
        host: heroes-svc-xsrk4.heroes-kn-ns.svc.cluster.local
        port:
          number: 80
      headers:
        request:
          add:
            Knative-Serving-Namespace: heroes-kn-ns
            Knative-Serving-Revision: heroes-svc-xsrk4
      weight: 100
    timeout: 10m0s
    websocketUpgrade: true
  - match:
    - authority:
        regex: ^v2-heroes-svc\.heroes-kn-ns(\.svc(\.cluster\.local)?)?(?::\d{1,5})?$
      gateways:
      - mesh
    retries:
      attempts: 3
      perTryTimeout: 10m0s
    route:
    - destination:
        host: heroes-svc-s7cv5.heroes-kn-ns.svc.cluster.local
        port:
          number: 80
      headers:
        request:
          add:
            Knative-Serving-Namespace: heroes-kn-ns
            Knative-Serving-Revision: heroes-svc-s7cv5
      weight: 100
    timeout: 10m0s
    websocketUpgrade: true
  - fault:
      abort:
        httpStatus: 200
        percent: 100
    match:
    - authority:
        exact: 594d18eea6863817fd83a52d4216e004.probe.invalid
    route:
    - destination:
        host: null.invalid
        port:
          number: 80
      weight: 0
```

## Consumir un servicio
Para consumir un servicio desde fuera del cluster:  

```
http://feheroes-svc.heroes-kn-ns.example.com/heroes
http://heroes-svc.heroes-kn-ns.example.com/heroes
http://heroes-last-svc.heroes-kn-ns.example.com/heroes
http://v1-heroes-svc.heroes-kn-ns.example.com/heroes
http://v2-heroes-svc.heroes-kn-ns.example.com/heroes
```
Satisfacen el `match` definido en el gateway asociado al ingress service.  

Desde el interior del cluster:  

```
http://heroes-last-svc.heroes-kn-ns/heroes
http://feheroes-svc.heroes-kn-ns/heroes
http://heroes-svc.heroes-kn-ns/heroes
http://heroes-last-svc.heroes-kn-ns/heroes
http://v1-heroes-svc.heroes-kn-ns/heroes
http://v2-heroes-svc.heroes-kn-ns/heroes
```

Satisfacen el `match` definido en el gateway predefinido `mesh`, que define el trafico generado desde los sidecars.  
