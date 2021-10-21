# guicey-cache
A bundle to cache the method response or remove cache with a custom key using method params or name. The key is formed 
based on the json paths defined in the annotation. This bundle intercepts all the calls to the method and checks if any 
stored cache object is present. If any then the method isn't invoked and the response is returned else the method is 
invoked and then cached for a pre-defined ttl

## When to Use it
It is generally useful when we need to call external clients multiple times and we know for a specific parameters, 
the response would be similar. For ex.
- When we need to make sequential calls to provider and the second call is dependent on the first call and the flow is 
async i.e. need app interventions to trigger second call
- To cache provider specific tokens which expires on some ttl based and is to be used in every call till then

## How to Use it 
- Clients need to implement the storage layer which would implement the *StoredCacheDao* interface
- Since the caching works on forming the key using the json path value, while compiling it's needed to retain the 
method arguments variables as it is instead of javac changing it to random names like *var0, var1, ...* If you're using 
maven compiler then the following would help i.e. addition of *compilerargs* in the configuration:

___
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
            <compilerArgs>
                <arg>-parameters</arg>
            </compilerArgs>
            <source>${source.version}</source>
            <target>${target.version}</target>
        </configuration>
    </plugin>
___

- Addition of this bundle in the Guice bundle initialization. 
___
    For Guice Bundle 4.x
    
    We need to add the below to let the Guice Bundle find out this bundle 
    configureFromDropwizardBundles()
___
___
    For Guice Bundle 5.x
    
    
___


## Other Features
- To force remove the cache entry either for a specific key or a group of keys. There can be a use case where 
the cache is to be removed when a different method is called.
- Compression and Decompression before storage to reduce storage
- If at any given point of time, the response object changes then we would need to remove the cache and hence we can 
define the timestamp at which the structural change happened in *structureChangeAt* which would lead to skip all 
the cache stored previously
- We can also encrypt while caching the response using different encryption mechanisms. 
Current supported encryption is AES 128 bit. It is also version based, as in we would want to change the 
encryption mechanism in future then all existing ones would still be decrypted using the earlier mechanism and 
is backward compatible


For other details, refer the tests