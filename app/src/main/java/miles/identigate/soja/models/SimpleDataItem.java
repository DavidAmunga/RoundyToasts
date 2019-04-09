package miles.identigate.soja.models;

public class SimpleDataItem {

	public String name;
	public String textValue;
	public int validity;
	public String Reserved2;

	public SimpleDataItem(){
		name="";
		textValue="";
		validity=0;
		Reserved2="";
	}

	public SimpleDataItem(String name, String textValue){
		this.name=name;
		this.textValue=textValue;
		validity=0;
		Reserved2="";
	}
}
