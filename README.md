# registry-regulations-cli

### Overview

* Command-line utility designed for verifying and securely storing the state of files in an
  OpenShift secret, as well as for validating registry regulation files in accordance with
  predefined validation rules.

### Usage

```bash
Usage [plan/save] command:
java -jar registry-regulations-cli.jar -DOPENSHIFT_NAMESPACE=name plan update-bp-grouping --file=/bp-grouping
java -jar registry-regulations-cli.jar -DOPENSHIFT_NAMESPACE=name save update-bp-grouping --file-detailed=/bp-grouping
Options:
    --file=<arg>            List of folders and files (accepts multiple values separated by ',')
    --file-detailed=<arg>   List of folders and files, provides detailed information (accepts multiple values separated by ',')

Usage [validate] command:
java -jar registry-regulations-cli.jar validate [--bp-auth-files=<arg>] [--bp-grouping-files=<arg>]
Options:
    --bp-auth-files=<arg>                        BP authorization regulation files (accepts multiple values separated by ',')
    --bp-grouping-files=<arg>                    Business process grouping file
    --ext-integrations-outbound=<arg>                   Configuration of outbound registry integrations
    --ext-integrations-inbound=<arg>                    Configuration of inbound registry integrations (accepts multiple values separated by ',')
    --bpmn-files=<arg>                           Business processes regulation files (accepts multiple values separated by ',')
    --datafactory-settings-files=<arg>           Datafactory Settings regulation files with yml, yaml extensions
    --diia-notification-template-folder=<arg>    Diia notification template directory
    --dmn-files=<arg>                            Business rules regulation files (accepts multiple values separated by ',')
    --email-notification-template-folder=<arg>   Email notification template directory
    --excerpt-folders=<arg>                      Folders that contain excerpts in different formats
    --form-files=<arg>                           UI forms regulation files (accepts multiple values separated by ',')
    --global-vars-files=<arg>                    Global variables regulation files (accepts multiple values separated by ',')
    --inbox-notification-template-folder=<arg>   Inbox notification template directory
    --liquibase-files=<arg>                      Liquibase regulation files introduce Database change set with xml extensions
    --mock-integration-files=<arg>               Mock integration regulation files (accepts multiple values separated by ',')
    --registry-settings-files=<arg>              Registry Settings regulation files with yml, yaml extensions
    --report-folders=<arg>                       Folders with reports files (accepts multiple values separated by ',')
    --reports-files=<arg>                        Reports files (accepts multiple values separated by ',')
    --roles-files=<arg>                          Authorization roles regulation files (accepts multiple values separated by ',')
    --assets-folder=<arg>                        Folder with assets files

Usage [help] command:
java -jar registry-regulations-cli.jar help

Exit codes: 0 (success), 1 (system error), 10 (validation failure)
```

### Test execution

* Tests could be run via maven command:
    * `mvn verify` OR using appropriate functions of your IDE.
    
### License

The registry-regulations-validation-cli is Open Source software released under
the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).