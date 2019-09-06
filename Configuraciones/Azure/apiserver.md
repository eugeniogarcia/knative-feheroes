# Connect to API Server
```
./kubectl proxy --port=8080 &

curl http://localhost:8080/api/
```
## Grant access to the cluster
We grant access to the cluster resources (to avoid getting a 403):  

```
./kubectl create clusterrolebinding acceso-total-cluster --clusterrole=cluster-admin --user=system:serviceaccount:default:default
```

## Check all possible clusters, as you .KUBECONFIG may have multiple contexts:
```
./kubectl config view -o jsonpath='{"Cluster name\tServer\n"}{range .clusters[*]}{.name}{"\t"}{.cluster.server}{"\n"}{end}'
```

## Select name of cluster you want to interact with from above output:
```
export CLUSTER_NAME="miKCluster"
```

## Point to the API server refering the cluster name
```
APISERVER=$(./kubectl config view -o jsonpath="{.clusters[?(@.name==\"$CLUSTER_NAME\")].cluster.server}")
```

## Gets the token value
```
TOKEN=$(./kubectl get secrets -o jsonpath="{.items[?(@.metadata.annotations['kubernetes\.io/service-account\.name']=='default')].data.token}"|base64 -d)
```

# Explore the API with TOKEN
## Core APIs
```
curl -X GET $APISERVER/api --header "Authorization: Bearer $TOKEN" --insecure

curl -X GET $APISERVER/api/v1 --header "Authorization: Bearer $TOKEN" --insecure
```

The pattern to retrieve the information for core APIs is:  

```
curl -X GET $APISERVER/api/v1/namespaces/<namespace-name>/<resource-type-name>/<resource-name> --header "Authorization: Bearer $TOKEN" --insecure
```

Some examples:  

```
curl -X GET $APISERVER/api/v1/nodes/ --header "Authorization: Bearer $TOKEN" --insecure |more

curl -X GET $APISERVER/api/v1/namespaces/heroes-ns/pods/ --header "Authorization: Bearer $TOKEN" --insecure |more

curl -X GET $APISERVER/api/v1/namespaces/heroes-ns/pods/heroes-v1-65dd6d45f5-q22bm --header "Authorization: Bearer $TOKEN" --insecure |more
```

## APIs by APIGroup
```
curl -X GET $APISERVER/apis --header "Authorization: Bearer $TOKEN" --insecure|more
```

The pattern to retrieve the information for core APIs is:

```
curl -X GET $APISERVER/apis/<api-group>/<api-version>/namespaces/<namespace-name>/<resource-type-name>/<resource-name> --header "Authorization: Bearer $TOKEN" --insecure|more
```  

Some examples:  

```
curl -X GET $APISERVER/apis/serving.knative.dev/v1alpha1/ --header "Authorization: Bearer $TOKEN" --insecure|more

curl -X GET $APISERVER/apis/serving.knative.dev/v1alpha1/namespaces/heroes-kn-ns/configurations --header "Authorization: Bearer $TOKEN" --insecure|more

curl -X GET $APISERVER/apis/serving.knative.dev/v1alpha1/namespaces/heroes-kn-ns/configurations/feheroes-svc --header "Authorization: Bearer $TOKEN" --insecure|more
```
# Revoke access to the cluster
We revoke the access we created earlier:  

```
./kubectl delete clusterrolebinding acceso-total-cluster 
```
If we run now the same request as before, we would get a `403`:  

```
curl -X GET $APISERVER/apis/serving.knative.dev/v1alpha1/namespaces/heroes-kn-ns/configurations/feheroes-svc --header "Authorization: Bearer $TOKEN" --insecure|more

{
  "kind": "Status",
  "apiVersion": "v1",
  "metadata": {

  },
  "status": "Failure",
  "message": "configurations.serving.knative.dev \"feheroes-svc\" is forbidden: User \"system:serviceaccount:default:default\" cannot g
et resource \"configurations\" in API group \"serving.knative.dev\" in the namespace \"heroes-kn-ns\"",
  "reason": "Forbidden",
  "details": {
    "name": "feheroes-svc",
    "group": "serving.knative.dev",
    "kind": "configurations"
  },
  "code": 403
}
```