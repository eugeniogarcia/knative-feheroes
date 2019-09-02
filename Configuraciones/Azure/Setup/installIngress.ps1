#Requires Helm.
#Run configureHelp.ps1 is Helm is not installed. This script will use helm-rbac.yaml

kubectl create namespace ingress-basic

helm install stable/nginx-ingress --namespace ingress-basic --set controller.replicaCount=2 --set controller.nodeSelector."beta\.kubernetes\.io/os"=linux --set defaultBackend.nodeSelector."beta\.kubernetes\.io/os"=linux

# Two pods are created that implement the Ingress...
kubectl get pods --namespace ingress-basic -o wide
# Plus a LoadBalancer service
kubectl get service -l app=nginx-ingress --namespace ingress-basic
