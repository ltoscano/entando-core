/*
 * Copyright 2015-Present Entando Inc. (http://www.entando.com) All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.agiletec.plugins.jacms.aps.system.services.resource.model;

import com.agiletec.aps.system.exception.ApsSystemException;
import com.agiletec.plugins.jacms.aps.system.services.resource.parse.ResourceDOM;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe astratta di base per l'implementazione 
 * di oggetti Risorsa Multi-Istanza.
 * @author E.Santoboni
 */
public abstract class AbstractMultiInstanceResource extends AbstractResource {

	private static final Logger _logger = LoggerFactory.getLogger(AbstractMultiInstanceResource.class);
	
	/**
	 * Inizializza la mappa delle istanze della risorsa.
	 */
	public AbstractMultiInstanceResource() {
        super();
        this._instances = new HashMap<String, ResourceInstance>();
    }
    
	@Override
	public void deleteResourceInstances() throws ApsSystemException {
		try {
			Collection<ResourceInstance> instances = this.getInstances().values();
			Iterator<ResourceInstance> instancesIter = instances.iterator();
			while (instancesIter.hasNext()) {
				ResourceInstance currentInstance = instancesIter.next();
				String fileName = currentInstance.getFileName();
				String subPath = this.getDiskSubFolder() + fileName;
				this.getStorageManager().deleteFile(subPath, this.isProtectedResource());
			}
		} catch (Throwable t) {
			_logger.error("Error on deleting resource instances", t);
			throw new ApsSystemException("Error on deleting resource instances", t);
		}
	}
    
    /**
     * Implementazione del metodo isMultiInstance() di AbstractResource.
     * Restituisce sempre true in quanto questa classe astratta è 
     * alla base di tutte le risorse MultiInstance.
     * @return true in quanto la risorsa è MultiInstance. 
     */
	@Override
	public boolean isMultiInstance() {
    	return true;
    }
    
    /**
     * Restituisce l'xml completo della risorsa.
     * @return L'xml completo della risorsa.
     */
	@Override
	public String getXML() {
    	ResourceDOM resourceDom = this.getResourceDOM();
    	List<ResourceInstance> instances = new ArrayList<ResourceInstance>(this.getInstances().values());
    	for (int i=0; i<instances.size(); i++) {
    		ResourceInstance currentInstance = instances.get(i);
    		resourceDom.addInstance(currentInstance.getJDOMElement());
    	}
    	return resourceDom.getXMLDocument();
    }
    
    /**
	 * Restituisce il nome corretto del file con cui un'istanza di una risorsa viene salvata nel fileSystem. 
	 * @param masterFileName Il nome del file principale da cui ricavare l'istanza.
	 * @param size Il size dell'istanza della risorsa multiInstanza.
	 * @param langCode Il codice lingua dell'istanza della risorsa multiInstanza.
	 * @return Il nome corretto del file.
	 * @deprecated Use getNewInstanceFileName
	 */
	public abstract String getInstanceFileName(String masterFileName, int size, String langCode);
	
    /**
	 * Restituisce il nome corretto del file con cui un'istanza di una risorsa viene salvata nel sistema. 
	 * @param masterFileName Il nome del file principale da cui ricavare l'istanza.
	 * @param size Il size dell'istanza della risorsa multiInstanza.
	 * @param langCode Il codice lingua dell'istanza della risorsa multiInstanza.
	 * @return Il nome corretto del file.
	 */
	protected String getNewInstanceFileName(String masterFileName, int size, String langCode) {
		StringBuilder fileName = null;
		do {
			String baseName = super.getNewUniqueFileName(masterFileName);
			fileName = new StringBuilder(baseName);
			String extension = super.getFileExtension(masterFileName);
			if (size >= 0) {
				fileName.append("_d").append(size);
			}
			if (langCode != null) {
				fileName.append("_").append(langCode);
			}
			if (StringUtils.isNotEmpty(extension)) {
				fileName.append(".").append(extension);
			}
		} while (this.exists(fileName.toString()));
		return fileName.toString();
	}
	
	/**
	 * Restituisce un'istanza della risorsa.
	 * @param size Il size dell'istanza della risorsa multiInstanza.
	 * @param langCode Il codice lingua dell'istanza della risorsa multiInstanza.
	 * @return L'istanza cercata.
	 */
	public abstract ResourceInstance getInstance(int size, String langCode);
	
    /**
     * Restituisce la mappa delle istanze della risorsa, 
     * indicizzate in base al size o alla lingua dell'istanza.
     * @return La mappa delle istanze della risorsa.
     */
    public Map<String, ResourceInstance> getInstances() {
    	return this._instances;
    }
    
    private Map<String, ResourceInstance> _instances;
	
}
