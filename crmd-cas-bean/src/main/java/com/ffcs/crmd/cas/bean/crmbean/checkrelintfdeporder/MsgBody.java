/*
 * This class was automatically generated with 
 * <a href="http://www.castor.org">Castor 1.3.3</a>, using an XML
 * Schema.
 * $Id$
 */

package com.ffcs.crmd.cas.bean.crmbean.checkrelintfdeporder;

/**
 * 消息体
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings("serial")
public class MsgBody implements java.io.Serializable {

    /**
     * Internal choice value storage
     */
    private java.lang.Object _choiceValue;

    /**
     * 请求信息
     */
    private com.ffcs.crmd.cas.bean.crmbean.checkrelintfdeporder.InParam inParam;

    /**
     * 响应信息
     */
    private com.ffcs.crmd.cas.bean.crmbean.checkrelintfdeporder.OutParam outParam;

    public MsgBody() {
        super();
    }

    /**
     * Returns the value of field 'choiceValue'. The field
     * 'choiceValue' has the following description: Internal choice
     * value storage
     * 
     * @return the value of field 'ChoiceValue'.
     */
    public java.lang.Object getChoiceValue() {
        return this._choiceValue;
    }

    /**
     * Returns the value of field 'inParam'. The field 'inParam'
     * has the following description: 请求信息
     * 
     * @return the value of field 'InParam'.
     */
    public com.ffcs.crmd.cas.bean.crmbean.checkrelintfdeporder.InParam getInParam() {
        return this.inParam;
    }

    /**
     * Returns the value of field 'outParam'. The field 'outParam'
     * has the following description: 响应信息
     * 
     * @return the value of field 'OutParam'.
     */
    public com.ffcs.crmd.cas.bean.crmbean.checkrelintfdeporder.OutParam getOutParam() {
        return this.outParam;
    }

    /**
     * Sets the value of field 'inParam'. The field 'inParam' has
     * the following description: 请求信息
     * 
     * @param inParam the value of field 'inParam'.
     */
    public void setInParam(final com.ffcs.crmd.cas.bean.crmbean.checkrelintfdeporder.InParam inParam) {
        this.inParam = inParam;
        this._choiceValue = inParam;
    }

    /**
     * Sets the value of field 'outParam'. The field 'outParam' has
     * the following description: 响应信息
     * 
     * @param outParam the value of field 'outParam'.
     */
    public void setOutParam(final com.ffcs.crmd.cas.bean.crmbean.checkrelintfdeporder.OutParam outParam) {
        this.outParam = outParam;
        this._choiceValue = outParam;
    }

}
