package com.axelor.rh.service;

import com.axelor.exception.AxelorException;

import java.io.Serializable;
import java.util.Map;

public interface ReportServiceGenerator {

    public String generateMany(String rptDesign, String outputName, String format, Map<String,Object> params) throws AxelorException;

    public String generateOne(String rptDesign, String outputName, String formats, Map<String,Object> params) throws AxelorException;

}