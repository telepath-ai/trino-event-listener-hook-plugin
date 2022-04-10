### Required Pulumi config values:

* `aws:region`
* `github:owner` - repository owner, i.e. "telepath-ai"
* `githubRepo` - the name of the repo holds the plugin

### Deploying Resources

This project creates secrets in GitHub so it requires a GitHub personal access token that has permission to 
create the secrets. This token can be passed as an environmental variable:

```
GITHUB_TOKEN=xxxx pulumi up
```