# LayerX - Jahia Digital Factory Project

LayerX - Jahia Digital Factory Project is the maven project contained source codes specifically for Jahia.

## Documentation

 * Read our [official documentation](http://layerx.technologies.io/docs/) for more information.
 
## Prerequisites

 * [LayerX - Parent Project](https://github.com/layerx/Parent)
 * [LayerX - API Project](https://github.com/layerx/API)
 * [LayerX - Core Project](https://github.com/layerx/Core)
 * Java 8
 * Jahia 7.2 or later (for integration with Jahia)

## License

Read [License](LICENSE) for more licensing information.

## Contribute

Read [here](CONTRIBUTING.md) for more information.

## Compile

    mvn clean install

## Deploy to Jahia

Edit your Maven settings.xml (usually in ~/.m2/settings.xml) to add the following profile :

        <profile>
            <id>jahia-local</id>
            <properties>
                <jahia.deploy.targetServerType>tomcat</jahia.deploy.targetServerType>
                <jahia.deploy.targetServerVersion>7</jahia.deploy.targetServerVersion>
                <jahia.deploy.targetServerDirectory><path_to_your_digital_factory_folder>/tomcat</jahia.deploy.targetServerDirectory>
            </properties>
        </profile>

Make sure you modify the jahia.deploy.targetServerDirectory to point to the /tomcat directory inside your Jahia installation.

Then you should be able to deploy your module using the following Maven command:

    mvn clean install jahia:deploy -P jahia-local
