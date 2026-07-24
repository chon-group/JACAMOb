package sai.main.lang.semantics;

import jason.infra.local.LocalRuntimeServices;

public abstract class SaiElement {

	private String metadata = "";

	public SaiElement() {
		super();
		neck.util.Trace.logSuper("super: xxxxxxxxxxxxxxxxxxx "+ LocalRuntimeServices.class.getSuperclass().getName());
	}

	public String getMetadata() {
		return metadata;
	}

	public void setMetadata(String metadata) {
		this.metadata = metadata;
	}
	
	
}
