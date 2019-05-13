package io.github.che4.i18n.menu.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.ParameterValuesException;
import org.eclipse.e4.ui.model.application.commands.MCommandParameter;

public class E4ModelUtil {
	
	public static IParameter[] convertToCoreParams(Collection<MCommandParameter> mParams) {
		Objects.requireNonNull(mParams);
		return mParams.stream()
			.map( mcp -> (IParameter) new IParameterImpl(mcp.getElementId(), mcp.getName(), mcp.isOptional()) )
			.toArray( IParameter[]::new );
	}
	
	
	static class IParameterImpl implements IParameter{
		private String id;
		private String name;
		private boolean isOptional;
		
		IParameterImpl(String id, String name){
			this(id, name, true);
		}
		
		IParameterImpl(String id, String name, boolean isOptional){
			this.id = id;
			this.name = name;
			this.isOptional = isOptional;
		}

		@Override public String getId() {return id;}
		@Override public String getName() { return name;}
		@Override public IParameterValues getValues() throws ParameterValuesException {
			return new IParameterValuesImpl();
		}
		@Override public boolean isOptional() { return isOptional; }
		
	}
	
	static class IParameterValuesImpl implements IParameterValues {
		@SuppressWarnings("rawtypes")
		@Override public Map getParameterValues() { return Collections.emptyMap();}
		
	}

}
