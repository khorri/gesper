package com.axelor.rh.service;

import com.axelor.config.db.Decision;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.rpc.Context;

import java.io.Serializable;

/**
 * Created by HB on 02/08/2018.
 */
public interface IDecisionWorkFlow extends Serializable {

    Model create(Model entity);

    Decision verify(Model entity);

    boolean validate(Model entity, Context context) throws AxelorException;

    boolean refuse(Model entity, Decision decision);

}
