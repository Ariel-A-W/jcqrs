`Fecha: 22-07-2025` `Versión: 1.0.0`

`Desarrollador: Ariel Alejandro Wagner`

# CQRS para Java

Depenencia para disponer del uso del patrón CQRS sobre proyectos desarrollados bajo el lenguaje Java EE. 

## Fundamentos de CQRS 

CQRS (***Command and Query Responsability Segregation***) o Segregación de Responsabilidades de Comandos y Consultas se trata de un patrón que permite separar las operaciones de lectura y escritura durante la gestión de persistencia de datos. Una de sus principales ventajas es que permite obtener un excelente rendimiento, escalabilidad y seguridad. Esta arquitectura tan particular tiene la gran ventaja de separar los procesos de escritura y lectura, minimizando de esta forma potenciales tipos de conflictos de fusión a nivel de dominio.

## Propuesta de CQRS 

Para poder resolver todas estas cuestiones en los sistemas tradicionales, el patrón CQRS nos permite superar muchos de estos problemas y, a su vez, nos permite establecer una mayor confiabilidad durante el diseño e implementación para los procesos de escala, mantenimiento, rendimiento, etc.

Como CQRS separa los universos de las consultas de lectura y escritua, este patrón plantea una nueva arquitectura interesante. Lo más destacado es la gestión que podría darse en las fuentes de persistencia de datos. Es decir, CQRS podría trabajar con dos tipos de fuentes de datos distintas para mejorar su rendimiento. Una fuente de datos podría ser la encargada de gestionar las lecturas y la otra las escrituras. Ambas fuentes de datos deberían encontrarse bajo un esquema de replicación para mantener consistente los datos. Este CQRS junto a otras aplicaciones adicionales de terceros, pueden gestionar muy eficazmente todos estos procesos. 

![Figura 1.](/docs/figura_1.png "Figura 1 - CQRS y el Patrón MediatR.")

Además la tecnología que proporciona CQRS admite el uso de colas para gestionar todas las consultas. Las colas permiten administrar cada uno de los procesos ejecutados en el contexto. Esto imprime directamente una mejor performance operativa.

### Comandos (Command)

Los comandos se basan en tareas. Esto lo podemos entender con la siguiente analogía; "Reservar una mesa en el restaurant" no es similar a "Establecer un lugar reservado en dicho restaurant". Este proceso considera directamente algunos cambios en el tipo o estilo de interacción con el usuario. Modificar la lógica del negocio que procesa dichos comandos tiene el objeto de que se logre la mayor cantidad de operaciones con mayor frecuencia y de manera exitosa. Esto se logra mediante la ejecución de diversas reglas desde el cliente incluso antes del envío de los comandos. Esto permite atajar todo tipo de excepciones o retardo en los procesos operativos. Dicho de otro modo, volviendo a nuestra analogía inicial del restaurant, esto explicaría el estado de la interface del usuario con una acción de "no quedan mesas". Todo este tipo de validaciones y reglas permiten controlar potenciales conflictos y errores en el servidor, evitando que este colapse por los conflictos y errores producidos en las operaciones del ambiente del sistema.

Por otro lado, los comandos pueden hacer uso de las colas mediante la modalidad asincrónica para mejorar cada una de las operaciones pertinentes. La modalidad asincrónica permite una mejor gestión de los recursos y eso se traduce en respuestas más rápidas y ágiles. Esto se ve reflejado tanto en las solicitudes como en las respuestas aunque es en las respuestas donde podremos observar mejor sus resultados.

### Consultas (Query)

Las consultas no modifican las bases de datos y por lo tanto, los datos se mantienen consistentes todo el tiempo. El rendimiento más óptimo es mantener en menoria las consultas mediante el uso de una caché. La agilidad de las respuestas de las consultas tienen su cede de rendimiento en este punto. Generalmente, las consultas de lectura suelen ser más lentas y consumir más recursos que la de escritura. Por este motivo se suelen destinar las consultas para que sean operativas en una base de datos a parte de la base de datos de persistencia. Bajo este escenario, se establecen dos tipos de fuentes de datos. Una utilizada para la lectura, la que hará uso de tecnologías que impriman mejor rendimiento como las bases de datos NoSQL o las orientadas a objetos como MongoDB. Mientras que las de escritura normalmente serán tipo de base de datos basadas en SQL relacionales tales como Microsoft SQL Server, Oracle Database, MySQL, PostgreSQL, etc. La separación permite lograr un mayor rendimiento general del sistema. Ver figura 2.

![Figura 2.](/docs/figura_2.png "Figura 2 - CQRS y el Patrón MediatR - Haciendo uso de dos Bases de Datos.")

#### Implementación CQRS + Mediator en Java EE con Spring Boot

Este ejemplo muestra una implementación clara y extensible del patrón CQRS (***Command Query Responsibility Segregation***) en Java, combinando interfaces de solicitudes, manejadores (***handlers***) y un mediador centralizado, inspirado en MediatR de .NET.

##### Interface IRequest<TResponse>

```java
public interface IRequest<TResponse> { }
```

Esta interface actúa como contrato base para cualquier tipo de solicitud dentro del sistema. Cada comando o consulta que quieras enviar a través del Mediator debe implementar esta interfaz parametrizada por el tipo de respuesta esperada (**TResponse**).

Por ejemplo, podrías tener:

```java
public class GetClienteByIdRequest implements IRequest<ClienteResponse> {
    private int id;
    // Constructor, getters, setters. Si aplica.
}
```

##### Interface IRequestHandler<TRequest, TResponse>

```java
public interface IRequestHandler<TRequest extends IRequest<TResponse>, TResponse> {
    TResponse handle(TRequest request);

    Class<?> getRequestType();

    default boolean canHandle(Object request) {
        return getRequestType().isAssignableFrom(request.getClass());
    }
}
```

Esta interface define la lógica de negocio de los "***handlers***" o manejadores para cada tipo de solicitud. Cada clase que implemente esta interface se encargará de resolver una solicitud concreta.

* ***handle()*** contiene la lógica principal.
* ***getRequestType()*** indica qué tipo de solicitud maneja la clase.
* ***canHandle()*** permite al Mediator saber si el handler puede procesar una instancia de solicitud específica.

Por ejemplo:

```java
@Service
public class GetClienteByIdHandler implements IRequestHandler<GetClienteByIdRequest, ClienteResponse> {

    @Override
    public ClienteResponse handle(GetClienteByIdRequest request) {
        // Lógica de negocio para buscar el cliente por ID
        return new ClienteResponse(...);
    }

    @Override
    public Class<?> getRequestType() {
        return GetClienteByIdRequest.class;
    }
}
```

##### Componente Mediator

```java
@Component
public class Mediator {
    @Autowired
    private ApplicationContext context;

    public <TRequest extends IRequest<TResponse>, TResponse> TResponse send(TRequest request) {
        Map<String, IRequestHandler> beans = context.getBeansOfType(IRequestHandler.class);

        for (IRequestHandler handler : beans.values()) {
            if (handler.canHandle(request)) {
                return ((IRequestHandler<TRequest, TResponse>) handler).handle(request);
            }
        }

        throw new RuntimeException("No se encontró un handler que pueda procesar: " + request.getClass().getSimpleName());
    }
}
```

Por empezar, Mediator es el núcleo del patrón. Este se encarga de encontrar el ***handler*** adecuado para una solicitud y delegar su ejecución.
Utiliza el **ApplicationContext** de Spring para buscar dinámicamente todos los beans registrados que implementan **IRequestHandler**.
Al recibir una solicitud (***send(request)***), itera sobre los handlers disponibles y usa ***canHandle()*** para identificar el adecuado. Si lo encuentra, ejecuta ***handle(request)*** y devuelve la respuesta. Si no encuentra uno válido, lanza una excepción.

##### Configuración de Alias: RequestHandlerAliasConfig

```java
@Configuration
public class RequestHandlerAliasConfig implements BeanDefinitionRegistryPostProcessor {
    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) {
        // Not Implemented
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] beanNames = beanFactory.getBeanNamesForType(IRequestHandler.class);

        for (String beanName : beanNames) {
            Class<?> type = beanFactory.getType(beanName);

            if (type != null) {
                String aliasName = buildAliasName(type.getSimpleName());
                if (!beanFactory.containsBean(aliasName)) {
                    ((BeanDefinitionRegistry) beanFactory).registerAlias(beanName, aliasName);
                    System.out.println("🔗 Alias registrado: " + aliasName + " → " + beanName);
                }
            }
        }
    }

    private String buildAliasName(String className) {
        if (className.endsWith("Handler")) {
            return Character.toLowerCase(className.charAt(0)) +
                    className.substring(1).replace("Handler", "RequestHandler");
        }
        return className;
    }
}
```

Este componente de configuración avanzada mejora la capacidad de Spring para reconocer beans y generar aliases automáticos basados en convenciones.
Escanea todos los beans que implementan **IRequestHandler** y crea un alias para cada uno, transformando el nombre de clase del handler a un nombre más predecible. Por ejemplo, (***getClienteByIdRequestHandler***).

Esto puede facilitar la inyección manual o el registro de beans de forma más limpia en el contexto de Spring.

### Cómo Implementar y Utilizar CQRS 

Para poder hacer uso de este patrón es necesario contar con las dependencias adecuadas. Aquí en este repositorio las dependencias se las puede encontrar albergadas dentro de la carpeta **cqrs**. Sin embargo, podría ser mejor y útil se crea la librería en formato .jar para luego referenciarlo al proyecto. Esto en caso de querer evitar que se altere la dependencia o por cuestiones de derechos de autor. En este repositorio las expongo pública y abiertamente para que puedan ser analizadas y/o mejoradas si es el caso. 

>**Nota**: 
>En el caso de lo puedas mejorar, me gustaría que las compartas en mi repositorio y participes como colaborador para luego poder compartirlo con toda la comundad de Java. 

Como hemos estudiado el patrón CQRS hace uso de dos secciones importantes llamadas Comandos (**Command**) y Consultas (**Query**). Por lo tanto, comenzaremos creando dos carpetas llamadas Command y Query respectivamente. Además de estas dos carperas, crearemos dos más llamadas Request y Response. Una última carpeta, aunque esta es opcional pero podría ser recomendada en caso de que tengamos que trabajar con más de una entidad de datos. En consecuencia, el aspecto de scaffolder nos debería quedar como se muestra a continuación. 

```
Entidad
  |__ Command
  |__ Query
  |__ Request
  |__ Response
``` 
En la carpeta **Command** iremos añadir cada una de las operaciones de cambios tales como Altas, Bajas y Modificaciones. Por otro lado, en la carpeta **Query** añadiremos las operaciones que son de solo lectura tales como listados, buscar por Id, etc. Respecto a las carpetas **Request** y la carpeta **Response**, las reservamos para dar formato a los tipos de datos, tanto para los entrantes (***request***) como los salientes (***response***). La arquitectura limpia ***Architecture Clean*** y otras similares en lugar de utilizar los nombres de Rquest y Response, acuden a los nombres de In y Out respectivamente. En la arquitectura hexagonal ***Architecture Hexagonal*** estas áreas son conocidas como interfaces de entrada y salida o también, son llamadas ***puertos***. **Nota**: No confundir coin los puertos utilizados en el protocolo TCP. 

## Código de Implementación

Asumiendo que contamos con un proyecto preparado, tal es el caso del uso de Boot Spring versión 3.5.x o mayor, basado en Maven, podremos partir con la breve explicación de las áreas más importantes para la codificación. 

Por un lado tendremos que ir construyendo el código de las clases para cada una de las operaciones de Altas, Bajas, Modificaciones e incluso, las consultas de solo lectura. Todas estas tienen la nomenclatura que termina con el término de ***XXXRequest*** y se deben alojar en la carpeta **Request** de la entidad dada. Damos un breve ejemplo. Finalmente, en la carpeta **Response** tendremos que ubicar la clase utilizada para el formato de datos de salida como ***XXXResponse***. Veamos cómo quedaría el scaffolding. 

```
Clientes
  |__ Command
  |__ Query
  |__ Request
  |   |__ ClientesAllRequest.java
  |   |__ ClientesByIdRequest.java
  |   |__ ClientesAddRequest.java 
  |   |__ ClientesDeleteRequest.java 
  |   |__ ClientesUpdateRequest.java
  |__ Response
      |__ ClientesResponse.java
```
Una vez que tenemos una idea de cómo se estructura y se ordenan los recursos de cada una de las clases procedemos a conocer el código. 

#### Código para los Request

```java

// Clases para los Reuest de Consultas de Solo Lectura
public class ClientesAllRequest implements IRequest<ArrayList<ClientesResponse>> {    
}

public class ClientesByIdRequest implements IRequest<ClientesResponse> {
    public int id;

    public ClientesByIdRequest() {}

    public ClientesByIdRequest(int _id) {
        this.id = _id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

// Consultas para los cambios que son de tipo Escritura. 
public class ClientesAddRequest implements IRequest<Integer> {
    public String razon;
    public String direccion;
    public String email;
    public String movil;
    
    public ClientesAddRequest() {}

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMovil() {
        return movil;
    }

    public void setMovil(String movil) {
        this.movil = movil;
    }      
} 

public class ClientesDeleteRequest implements IRequest<Integer> {
    public int id;
    
    public ClientesDeleteRequest() {}

    public ClientesDeleteRequest(int _id) {
        this.id = _id;
    }    
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }  
}

public class ClientesUpdateRequest implements IRequest<Integer> {
    public long id;
    public String razon;
    public String direccion;
    public String email;
    public String movil;

    public ClientesUpdateRequest() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMovil() {
        return movil;
    }

    public void setMovil(String movil) {
        this.movil = movil;
    }
}

// Clase que otorga el formato de datos de salida. 
public class ClientesResponse {
    public int id;
    public String razon;
    public String direccion;
    public String email;
    public String movil;

    public ClientesResponse() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getRazon() {
        return razon;
    }

    public void setRazon(String razon) {
        this.razon = razon;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMovil() {
        return movil;
    }

    public void setMovil(String movil) {
        this.movil = movil;
    }
}
```

Los siguiente códigos serán utilizados para crear los ***casos de uso*** para la aplicación. En este punto vamos implementar la clase principal **Mediator** y también, cada una de las clases ubicadas en las carpetas **Request** y **Response**. 

Cada uno de los casos de usos utilizaran el manipulador llamado ***Handler***. El nombre de la clase manipuladora, que en esta ocasión se comporta como un caso de uso, la nomenclatura exige que su nombre termine con la palabra Handler. Por ejemplo, **ClientesAdd*****Handler*** para la clase de caso de uso que se encargaría de añadir un nuevo registro. Por lo tanto, cada uno de los código del CRUD (***Create Read Update Delete***) tendrán el siguiente código y nomenclatura. Cada comando y consulta son ubicados en las siguientes carpetas de la siguiente forma. 

```
Clientes
  |__ Command
  |   |__ ClientesAddCommand.java
  |   |__ ClientesDeleteCommand.java
  |   |__ ClientesUpdateCommand.jave
  |__ Query
  |   |__ ClientesAllHandler.java
  |   |__ ClientesByIdHandler.java
  |__ Request
  |   |__ ClientesAllRequest.java
  |   |__ ClientesByIdRequest.java
  |   |__ ClientesAddRequest.java 
  |   |__ ClientesDeleteRequest.java 
  |   |__ ClientesUpdateRequest.java
  |__ Response
      |__ ClientesResponse.java
```
El código se describe a continuación. 

```java
// Consulta Solo Lectura (Query) 
@Service
public class ClientesAllHandler implements IRequestHandler<ClientesAllRequest, ArrayList<ClientesResponse>> {

    @Override
    public ArrayList<ClientesResponse> handle(ClientesAllRequest request) {
        try {
            var lstClie = new ArrayList<ClientesResponse>();

            // 1
            var clie1 = new ClientesResponse();
            clie1.setId(1);
            clie1.setRazon("SOLITUX SRL");
            clie1.setDireccion("Av. San Juan 1234");
            clie1.setEmail("ventas@solutixsrl.com.ar");
            clie1.setMovil("11-4422-7755");
            lstClie.add(clie1);
            // 2
            var clie2 = new ClientesResponse();
            clie2.setId(2);
            clie2.setRazon("GRUPO TEXTIL SA");
            clie2.setDireccion("Rosario 7788");
            clie2.setEmail("info@grupo-textil.com.ar");
            clie2.setMovil("11-3344-9911");
            lstClie.add(clie2);
            // 3
            var clie3 = new ClientesResponse();
            clie3.setId(3);
            clie3.setRazon("ALUMINIUN METAL CO. SACIF");
            clie3.setDireccion("Triunvirato 8293");
            clie3.setEmail("info@aluminiummetal.com.ar");
            clie3.setMovil("11-3322-4275");
            lstClie.add(clie3);

            clie1 = null;
            clie2 = null;
            clie3 = null;

            return lstClie;
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Class<?> getRequestType() {
        return ClientesAllRequest.class;
    }
}

@Service
public class ClientesByIdHandler implements IRequestHandler<ClientesByIdRequest, ClientesResponse> {

    @Override
    public ClientesResponse handle(ClientesByIdRequest request) {
        try {
            var lstClie = new ArrayList<ClientesResponse>();

            // 1
            var clie1 = new ClientesResponse();
            clie1.setId(1);
            clie1.setRazon("SOLITUX SRL");
            clie1.setDireccion("Av. San Juan 1234");
            clie1.setEmail("ventas@solutixsrl.com.ar");
            clie1.setMovil("11-4422-7755");
            lstClie.add(clie1);
            // 2
            var clie2 = new ClientesResponse();
            clie2.setId(2);
            clie2.setRazon("GRUPO TEXTIL SA");
            clie2.setDireccion("Rosario 7788");
            clie2.setEmail("info@grupo-textil.com.ar");
            clie2.setMovil("11-3344-9911");
            lstClie.add(clie2);
            // 3
            var clie3 = new ClientesResponse();
            clie3.setId(3);
            clie3.setRazon("ALUMINIUN METAL CO. SACIF");
            clie3.setDireccion("Triunvirato 8293");
            clie3.setEmail("info@aluminiummetal.com.ar");
            clie3.setMovil("11-3322-4275");
            lstClie.add(clie3);

            clie1 = null;
            clie2 = null;
            clie3 = null;

            List<ClientesResponse> result = lstClie.stream()
                    .filter(p -> p.getId() == request.getId())
                    .collect(Collectors.toList());

            return result.get(0);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public Class<?> getRequestType() {
        return ClientesByIdRequest.class;
    }
}

// Consultas de Escritura (Command)
@Service
public class ClientesAddCommand implements IRequestHandler<ClientesAddRequest, Integer> {

    @Override
    public Integer handle(ClientesAddRequest request) {
        try {
            // Aquí se añade toda la lógica para producir el alta.
            System.out.println(request);            
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public Class<?> getRequestType() {
        return ClientesAddRequest.class;
    }    
}

@Service
public class ClientesDeleteCommand implements IRequestHandler<ClientesDeleteRequest, Integer> {

    @Override
    public Integer handle(ClientesDeleteRequest request) {
        try {
            // Aquí se añade toda la lógica para producir el borrado de datos.
            System.out.println(request);            
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public Class<?> getRequestType() {
        return ClientesDeleteRequest.class;
    }    
}

@Service
public class ClientesUpdateCommand implements IRequestHandler<ClientesUpdateRequest, Integer> {

    @Override
    public Integer handle(ClientesUpdateRequest request) {
        try {
            // Aquí se añade toda la lógica para producir la actualización de datos.
            System.out.println(request);            
            return 1;
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public Class<?> getRequestType() {
        return ClientesUpdateRequest.class;
    }
}
```

