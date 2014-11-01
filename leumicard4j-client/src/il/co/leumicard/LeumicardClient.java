package il.co.leumicard;

import il.co.leumicard.impl.CookieBuilder;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.UrlEncodedContent;

public class LeumicardClient {
	private final HttpRequestFactory requestFactory;
	private final Integer connectTimeout;
	private final Integer readTimeout;
	
	public LeumicardClient(HttpRequestFactory requestFactory, Integer connectTimeout, Integer readTimeout) {
		this.requestFactory = requestFactory;
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}
	
	public String login(Credentials credentials) throws IOException {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("username", credentials.username);
		params.put("password", credentials.password);
		
		final HttpRequest request = requestFactory.buildPostRequest(
				new GenericUrl("https://online.leumi-card.co.il/Anonymous/Login/CardHoldersLogin.aspx?sourceGA=myAccountMainBanner"),
				new UrlEncodedContent(params));
		if (connectTimeout != null) {
			request.setConnectTimeout(connectTimeout);
		}
		if (readTimeout != null) {
			request.setReadTimeout(readTimeout);
		}
		request.setFollowRedirects(false);
		request.setThrowExceptionOnExecuteError(false);
		
		final HttpResponse response = request.execute();
		try {
			final CookieBuilder builder = new CookieBuilder();
			final List<String> setCookies = response.getHeaders().getHeaderStringValues("set-cookie");
			for (String setCookie : setCookies) {
				final List<HttpCookie> cookies = HttpCookie.parse(setCookie);
				for (HttpCookie cookie : cookies) {
					builder.add(cookie.getName(), cookie.getValue());
				}
			}
			return builder.build();
		} finally {
			response.ignore();
		}
	}
	
	private Document retrieveTransactionsDocument(String cookie) throws IOException {
		final HttpRequest request = requestFactory.buildGetRequest(new GenericUrl("https://online.leumi-card.co.il/Popups/Print.aspx?PrintType=TransactionsTable&CardIndex=0&TableType=NisTransactions&ActionType=NextCharge&FilterParam=AllTranactions&CycleDate=&FromDate=&ToDate=&SortDirection=Ascending&SortParam=PaymentDate&NextBillingCycleDate=02/11/2014&LastStatementDate=02/10/2014"));
		if (connectTimeout != null) {
			request.setConnectTimeout(connectTimeout);
		}
		if (readTimeout != null) {
			request.setReadTimeout(readTimeout);
		}
		request.getHeaders().setCookie(cookie);
		
		final HttpResponse response = request.execute();
		try {
			return Jsoup.parse(response.parseAsString());
		} finally {
			response.ignore();
		}
	}
	
	private static Merchant parseMerchantElement(Element element) {
//		 <li><strong> פרטי בית העסק</strong> 
//		  <ul> 
//		   <li> <label> שם:</label> לאומי קארד פינוקים פלוס</li> 
//		   <li> <label> כתובת:</label> בן גוריון 11, בני ברק, 51260</li> 
//		   <li> <label> טלפון:</label> <a onclick="return false;" class="tel-link-mobile" href="tel:03-6178888">03-6178888</a> </li> 
//		   <li> <label> פקס:</label> </li> 
//		   <li> <label> ענף:</label> שירותי פנאי ונופש</li> 
//		  </ul> </li> 
		final Iterator<Element> it = element.select("ul > li").iterator();
		final String title = it.next().textNodes().get(1).toString().trim();
		final String address = it.next().textNodes().get(1).toString().trim();;
		it.next();
		final String phone = ""; // TODO
		it.next();
		final String fax = ""; // TODO
		final String category = it.next().textNodes().get(1).toString().trim();;
		
		return new Merchant(title, address, phone, fax, category);
	}
	
	private static Deal parseDealElement(Element element) {
//		 <li class="creditPeirut_left"><strong> פרטי עסקה</strong> 
//		  <ul> 
//		   <li> <label> סוג עסקה:</label> רגילה</li> 
//		   <li> <label> אופן ביצוע:</label> טלפוני </li> 
//		   <li> <label> מטבע:</label> ILS</li> 
//		   <li id="tbl1_lvTransactions_liOriginalAmount_32"> <label> סכום מקור:</label> <span id="tbl1_lvTransactions_lblOriginalAmount_32">‎ 38.00</span> </li> 
//		   <li id="tbl1_lvTransactions_liPurchaseAmount_32"> <label> <span id="tbl1_lvTransactions_Literal11_32">סכום ב- $</span>:</label> <label style="direction: ltr; text-align: right;"></label></li> 
//		   <li id="tbl1_lvTransactions_liInterestAmount_32"> <label> ריבית:</label> ‎ 0.00</li> 
//		   <li id="tbl1_lvTransactions_liAdjustmentAmount_32"> <label> <span id="tbl1_lvTransactions_Literal13_32">הפרשי שער</span>:</label> </li> 
//		   <li id="tbl1_lvTransactions_liPurchaseTime_32"> <label> שעת העסקה:</label> 17:00 17/10/2014</li> 
//		   <li id="tbl1_lvTransactions_liAmountOfPromotion_32"> <label> הנחת מועדון/נקודות:</label> <span>‎ </span> </li> 
//		   <li id="tbl1_lvTransactions_liExchangeRate_32"> <label> <span id="tbl1_lvTransactions_Literal17_32">שער המרה<br> מ$ לש''ח</span> : </label> 0.0000</li> 
//		   <li id="tbl1_lvTransactions_liProcessingDate_32"> <label> תאריך המרה:</label> 20/10/2014</li> 
//		   <li id="tbl1_lvTransactions_liExchangeCommission_32"> <label> עמלת המרה:</label> <label style="direction: ltr; text-align: right;"></label></li> 
//		   <li id="tbl1_lvTransactions_liWithdrawalCommission_32"> <label> עמלת משיכת<br> מזומן:</label>‎ </li> 
//		  </ul> </li> 
		final Iterator<Element> it = element.select("ul > li").iterator();
		final String type = it.next().textNodes().get(1).toString().trim();
		final String channel = it.next().textNodes().get(1).toString().trim();;
		final String currency = it.next().textNodes().get(1).toString().trim();;
		final String amount = it.next().select("span").first().text();
		it.next();
		it.next();
		it.next();
		final String time = it.next().textNodes().get(1).toString().trim();
		
		return new Deal(type, channel, currency, amount, time);
	}
	
	public List<Transaction> retrieveTransactions(String cookie) throws IOException {
		final Document doc = retrieveTransactionsDocument(cookie);
		
		final List<Transaction> transactions = new LinkedList<Transaction>();
		for (Element transactionElement : doc.select(".creditPeirut_opened")) {
			final Merchant merchant = parseMerchantElement(transactionElement.child(0));
			if (merchant.title.isEmpty()) {
				continue;
			}
			
			final Deal deal = parseDealElement(transactionElement.child(1));
			
			transactions.add(new Transaction(merchant, deal));
		}
			
		return transactions;
	}
}
