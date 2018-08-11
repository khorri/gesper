package com.axelor.rh.web;

import com.axelor.apps.report.engine.ReportSettings;
import com.axelor.exception.AxelorException;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rh.annotation.Avancement;
import com.axelor.rh.report.IReport;
import com.axelor.rh.service.ReportServiceGenerator;
import com.axelor.rh.utils.Utils;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.persist.Transactional;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.meta.db.MetaSelectItem;

public class ReportingController{


    @Inject
    @Avancement
    private ReportServiceGenerator reportService;

    @Inject
    private MetaSelectItemRepository itemRepository;

    @Transactional
    public void print(ActionRequest request, ActionResponse response) throws AxelorException {
        String typeReport= (String) request.getContext().get("typeReport");
        String name=getReportName(typeReport);
        typeReport+=".rptdesign";
        String fileLink = reportService.generateMany(typeReport,name, ReportSettings.FORMAT_PDF,generateParams(request));

        response.setView(ActionView
                .define(name)
                .add("html", fileLink).map());
    }

    private Map<String,Object> generateParams(ActionRequest request){
        String age1=request.getContext().get("startAge")!=null?request.getContext().get("startAge").toString():null;
        String age2=request.getContext().get("endAge")!=null?request.getContext().get("endAge").toString():null;
        String date=request.getContext().get("dateDebut")!=null?request.getContext().get("dateDebut").toString():null;

        Map<String, Object> params= new HashMap<String,Object>();
        if(age1!=null && !age1.isEmpty())
            params.put("age1",Integer.parseInt(age1));
        if(age2!=null && !age2.isEmpty())
            params.put("age2",Integer.parseInt(age2));
        if(date!=null && !date.isEmpty())
            params.put("date", Utils.convertString2Date(date,"yyyy-MM-dd"));
        return params;
    }

    private String getReportName(String key){
        MetaSelectItem item=itemRepository.all().filter("self.value=?1",key).fetchOne();
        return item==null?"Report ":item.getTitle();
    }
}