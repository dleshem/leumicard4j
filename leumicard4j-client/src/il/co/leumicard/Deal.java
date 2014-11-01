package il.co.leumicard;

public class Deal {
	public final String type;
	public final String channel;
	public final String currency;
	public final String amount;
	public final String time;
	
	public Deal(String type, String channel, String currency, String amount, String time) {
		this.type = type;
		this.channel = channel;
		this.currency = currency;
		this.amount = amount;
		this.time = time;
	}
}
