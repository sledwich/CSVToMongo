package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;


public class AppParameters {

	private boolean required;
	private boolean noValue;
	private String name;
	private String value;
	private Enum<?>[] options;

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}


	public AppParameters(List<AppParameters> params, final boolean required, final boolean bNoValue, final String name){
		this.setRequired(required);
		this.setNoValue(bNoValue);
		this.setName(name);
		params.add(this);
	}
	
	public AppParameters(List<AppParameters> params, final boolean required, final boolean bNoValue, final String name, final String defaultValue){
		this.setRequired(required);
		this.setNoValue(bNoValue);
		this.setName(name);
		params.add(this);
		setValue(defaultValue);
	}

	public AppParameters(ArrayList<AppParameters> params, final boolean required, final boolean bNoValue, final String name, final String defaultValue, final Enum<?>[] values){
		this.setRequired(required);
		this.setNoValue(bNoValue);
		this.setName(name);
		params.add(this);
		setValue(defaultValue);
		setOptions(values);
	}

	public boolean isNoValue() {
		return noValue;
	}

	public void setNoValue(boolean noValue) {
		this.noValue = noValue;
	}

	public String name() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Long toLong() {
		return Long.parseLong(value);
	}
	public boolean toBool() {
		return BooleanUtils.toBoolean(value);
	}

	public boolean isPresent() {
		return StringUtils.isNotEmpty(value);
	}

	public Enum<?>[] getOptions() {
		return options;
	}

	public void setOptions(Enum<?>[] options) {
		this.options = options;
	}
	
	public static HashMap<AppParameters, String> checkParam(String[] args, List<AppParameters> params) {
		boolean bThrowError = false;
		
		final HashMap<AppParameters, String> map = new HashMap<AppParameters, String>();
		for( String arg : args){
			boolean found = false;
			if( arg.startsWith("--"))
			{
				String value=null; 
				if( arg.indexOf('=') > 0 )
					value = arg.substring(arg.indexOf("=")+1);
				for( AppParameters param : params){
					final String fullparam="--"+param.name();
					final String[] words=org.apache.commons.lang3.StringUtils.split(arg, " =");
					
					boolean foundMatch = false;
					if( words != null && words.length>0 && words[0].compareTo(fullparam) == 0)
						foundMatch=true;
					
					if( arg.compareTo(fullparam)==0 )
						foundMatch=true;
					
					if( foundMatch )
					{
						if( param.isNoValue())
							value="TRUE";
						map.put(param, value);
						param.setValue(value);
						found=true;
					}
				}
			}
			if( !found)
			{
				bThrowError = true;
				System.err.println("Unknown parameter "+arg);
			}
		}
		for( AppParameters param: params){
			if( StringUtils.isBlank(map.get(param)) && param.isRequired())
				bThrowError = true;
		}

		if( bThrowError ){
			System.out.println("All possible options are: ");
			for( AppParameters param: params){
				
				
				String value = map.get(param);
				//OBSCURE PASSWORDS
				if( param.name().contains("pass") && value != null)
				{
					String newv = new String();
					for( int index=0; index < value.length(); index++)
						newv += (index>0 && index < (value.length()-1))?"*":value.charAt(index);
					value = newv;
				}
				String content = " --"+param.name() + (map.get(param)!=null?" (="+value+")":"");
				
				StringBuilder sb= null;
				if( param.getOptions()!=null) {
					sb = new StringBuilder();
					
					for( Enum<?>e:param.getOptions()) {
						if(sb.length()>0)
							sb.append(",");
						sb.append(e.name());
					}	
				}
				
				if( StringUtils.isBlank(map.get(param)) && param.isRequired())
					System.err.println(content+" "+(sb!=null?" possible values ("+sb+")":""));
				else
					System.out.println(content +" (optional) "+(sb!=null?" possible values ("+sb+")":""));
				
			}
			System.exit(-1);
		}
		return map;
	}

	public int toInt() {
		return Integer.parseInt(value);	
	}

}
