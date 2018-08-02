package com.axelor.rh.web;

import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Repository;
import com.axelor.exception.AxelorException;
import com.axelor.rh.service.IDecisionWorkFlow;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

/**
 * Created by HB on 02/08/2018.
 */
public class DecisionWorkFlowController {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Inject
    @Named("decision")
    IDecisionWorkFlow decisionWorkFlow;

    @Transactional
    public void verifie(ActionRequest request, ActionResponse response) throws AxelorException {
        Model model = getModel(request);
        decisionWorkFlow.verify(model);
    }

    private Model getModel(ActionRequest request) throws AxelorException {
        try {
            String modelString = request.getModel();
            Class model = Class.forName(modelString);
            final Repository repository = JpaRepository.of(model);
            List<Object> records = request.getRecords();
            List<Object> data = Lists.newArrayList();
            if (records == null) {
                records = Lists.newArrayList();
                records.add(request.getData());
            }
            for (Object record : records) {

                if (record == null) {
                    continue;
                }

                record = (Map) repository.validate((Map) record, request.getContext());

                Long id = findId((Map) record);

//                if (id == null || id <= 0L) {
//                    security.get().check(JpaSecurity.CAN_CREATE, model);
//                }

                Map<String, Object> orig = (Map) ((Map) record).get("_original");
                JPA.verify(model, orig);

                // save translatable values and remove them from record
//                Translator.saveTranslatables((Map) record, model);

                Model bean = JPA.edit(model, (Map) record);
                id = bean.getId();

//                if (bean != null && id != null && id > 0L) {
//                    security.get().check(JpaSecurity.CAN_WRITE, model, id);
//                }

                bean = JPA.manage(bean);
//                if (repository != null) {
//                    bean = repository.save(bean);
//                }

                // if it's a translation object, invalidate cache
//                if (bean instanceof MetaTranslation) {
//                    I18nBundle.invalidate();
//                }

                //data.add(repository.populate(toMap(bean), request.getContext()));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AxelorException();
        }
        return null;
    }

    private Long findId(Map<String, Object> values) {
        try {
            return Long.parseLong(values.get("id").toString());
        } catch (Exception e) {
        }
        return null;
    }
}
