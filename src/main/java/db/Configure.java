package db;

import org.yaml.snakeyaml.Yaml;
import java.io.InputStream;
import java.util.Map;

public class Configure {
    private Map<String, Object> configure;

    public Configure(){
        Yaml conf =  new Yaml();
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("conf.yaml");
        configure = conf.load(inputStream);
    }
    public String getPassword(){return String.valueOf(configure.get("password"));}
    public String getNameDB(){return String.valueOf(configure.get("nameDB"));}
    public String getHost(){return String.valueOf(configure.get("host"));}
    public String getPort(){return String.valueOf(configure.get("port"));}
    public String getUser(){return String.valueOf(configure.get("user"));}

}
