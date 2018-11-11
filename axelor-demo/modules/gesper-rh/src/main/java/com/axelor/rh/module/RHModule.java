package com.axelor.rh.module;

import com.axelor.app.AxelorModule;
import com.axelor.rh.service.CongeCalculator;
import com.axelor.rh.service.DecisionWorkFlowImpl;
import com.axelor.rh.service.IDecisionWorkFlow;
import com.google.inject.name.Names;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Set;

/**
 * Created by HORRI on 25/07/2018.
 */
public class RHModule extends AxelorModule {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    @Override
    protected void configure() {
        logger.info(" Configure RH Module... ");
        try {
            Reflections reflections = new Reflections("com.axelor.rh.service");
            Set<Class<? extends CongeCalculator>> allClasses =
                    reflections.getSubTypesOf(CongeCalculator.class);
            for (Class clazz : allClasses) {

                CongeCalculator obj = (CongeCalculator) clazz.newInstance();
                bind(CongeCalculator.class).annotatedWith(Names.named(obj.getCode())).to(clazz);
            }
        } catch (Exception e) {
            logger.error("Could not configure the HR module", e);
        }
        bind(IDecisionWorkFlow.class).to(DecisionWorkFlowImpl.class);

    }
}
