package il.co.leumicard;

public class Merchant {
	public final String title;
	public final String address;
	public final String phone;
	public final String fax;
	public final String category;
	
	public Merchant(String title, String address, String phone, String fax, String category) {
		this.title = title;
		this.address = address;
		this.phone = phone;
		this.fax = fax;
		this.category = category;
	}
}
