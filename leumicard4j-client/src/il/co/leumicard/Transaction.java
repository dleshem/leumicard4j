package il.co.leumicard;

public class Transaction {
	public final Merchant merchant;
	public final Deal deal;
	
	public Transaction(Merchant merchant, Deal deal) {
		this.merchant = merchant;
		this.deal = deal;
	}
}
