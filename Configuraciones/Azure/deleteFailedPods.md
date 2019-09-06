# Install azure cli
Follow the instructions for manual installation [in](https://docs.microsoft.com/en-us/cli/azure/install-azure-cli-apt?view=azure-cli-latest)

# Configure kubctl
Run the following:  
```
az login 

az aks get-credentials --resource-group miKubernetes --name miKCluster --admin
```

## Manual configuration
We can configure it manually, but is more complicated, we would need the certificates. These are the commands:  
```
./kubectl config set-cluster miKCluster --server=https://mikcluster-dns-0fd06581.hcp.westeurope.azmk8s.io:443 --certificate-authority=DATA+OMITTED


./kubectl config set-credentials clusterUser_miKubernetes_miKCluster --client-certificate=REDACTED --client-key=REDACTED  --token=EPEFpA3OiBHo3v5hnjPC3IPfWdiHSUWV1Ex9RWG4jFaj7RINnJdtx62dem6IQjiViqywNIjpF5o52y5EBJ2rkc9OzhUBuxmGvxSDcttT6bmpJ8DKAmZN13hKKXE3yYIv


./kubectl config  set-context miKCluster --cluster=miKCluster --user=clusterUser_miKubernetes_miKCluster


./kubectl config  use-context miKCluster 


./kubectl config view
```

# Delete failed pods
```
./kubectl get po --all-namespaces --field-selector 'status.phase==Failed' -o json | ./kubectl delete -f -
```