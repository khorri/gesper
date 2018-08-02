package ma.nawar.config.util;

import com.axelor.config.db.Parametre;
import com.axelor.config.db.repo.ParametreRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by HORRI on 27/07/2018.
 */
@Singleton
public class ParametersLoader {

    @Inject
    ParametreRepository parametreRepository;

    private final Map<String, String> parameters = new ConcurrentHashMap();

    private Map<String,String> load(){
        if(parameters != null && !parameters.isEmpty())
            return parameters;
        List<Parametre> params = parametreRepository.all().fetch();
        for(Parametre param: params ){
            parameters.put(param.getName(),param.getValeur());
        }
        return parameters;

    }

    public String get(String key){
        return load().get(key);
    }
}
