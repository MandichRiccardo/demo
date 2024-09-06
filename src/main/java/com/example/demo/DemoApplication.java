package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@SpringBootApplication
@RestController
public class DemoApplication {
	
	@RequestMapping("")
	public String home(){
		return home;
	}

	@GetMapping("/cod_articolo={codArticolo}")
	public String codArticolo(@PathVariable("codArticolo") String codArticolo){
		String body = "";
		codArticolo = "'" + codArticolo + "'";
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.8.170:3306/testresi", "testresi", "Sip3R§si")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                //execute query
                try (ResultSet rs = stmt.executeQuery("SELECT COD_ARTICOLO, DES_ARTICOLO from ana_articolo where cod_articolo = " + codArticolo)) {
					body = selectToString(rs, body);
				}
            }
		} catch (SQLException e) {
            throw new RuntimeException(e);
        }
		return body;
	}

	private static String selectToString(ResultSet rs, String body) throws SQLException{
		String[] tabel = new String[0];
		String label = "";
		for(int i = 1; i<= rs.getMetaData().getColumnCount(); i++) {
			label = label.concat("<td>" + rs.getMetaData().getColumnName(i) + "</td>");
		}
		tabel = addRow(tabel, label);
		while(rs.next()) {
			String row = "";
			for(int i = 1; i<= rs.getMetaData().getColumnCount(); i++) {
				row = row.concat("<td>" + rs.getString(i) + "</td>");
			}
			tabel = addRow(tabel, row);
		}
		body = body.concat("<table border=\"\">");
		for(String line:tabel) {
			body = body.concat("<tr>" + line + "</tr>");
		}
		body = body.concat("</table>");
		return body;
	}

	@GetMapping("/table={table}")
	public String table(@PathVariable("table") String table){
		String body;
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.8.170:3306/testresi", "testresi", "Sip3R§si")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                //execute query
                try (ResultSet rs = stmt.executeQuery("SELECT * from " + table)) {
                //try (ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA='testresi'")) {
                    //position result to first
                	body = selectToString(rs, "");
                }
            }
		} catch (SQLException e) {
            throw new RuntimeException(e);
        }
		return body;
    }

	@GetMapping("/codice_collo={codiceCollo}")
	public String codiceCollo(@PathVariable("codiceCollo") String codiceCollo){
		String body = "";
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.8.170:3306/testresi", "testresi", "Sip3R§si")) {
			try (Statement stmt = conn.createStatement()) {
				try (ResultSet rs1 = stmt.executeQuery("select * from segnacollo where " + codiceCollo)) {
					String codiceTipoResoMagazzino;
					if(rs1.next()){
						codiceTipoResoMagazzino = rs1.getString("COD_TIPO_RESO") + ";" + rs1.getString("COD_MAGAZZINO");
						try (ResultSet rs2 = stmt.executeQuery("select COD_TIPO_RESO, DESCRIZIONE from TAB_TIPO_RESO where COD_TIPO_RESO = '" + codiceTipoResoMagazzino.split(";")[0] + "' and COD_MAGAZZINO = '" + codiceTipoResoMagazzino.split(";")[1] + "'")){
							body = selectToString(rs2, body);
						}
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return body;
	}

	@GetMapping("codice_collo={codiceCollo}/cod_articolo={codArticolo}/serial={serial}")
	public String serial(@PathVariable("codiceCollo") String codiceCollo, @PathVariable("codArticolo") String codArticolo, @PathVariable("serial") String serial){
		String body = "";
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.8.170:3306/testresi", "testresi", "Sip3R§si")) {
			try (Statement stmt = conn.createStatement()) {
				int modifiche = stmt.executeUpdate("update segnacollo set SERIAL = '" + serial + "'" + "where CODICE_COLLO = '" + codiceCollo + "' and COD_ARTICOLO = '" + codArticolo + "';");
				body = body.concat(modifiche != 0 ? "true" : "false");
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return body;
	}

	@GetMapping("/codice_collo={codiceCollo}/cod_articolo={codArticolo}/qta_resa={qtaResa}")
	public String reso(@PathVariable("codiceCollo") String codiceCollo, @PathVariable("codArticolo") String codArticolo, @PathVariable("qtaResa") String qtaResa){
		String body;
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.8.170:3306/testresi", "testresi", "Sip3R§si")) {
			try (Statement stmt = conn.createStatement()) {
				try (ResultSet rs1 = stmt.executeQuery("select qta_prevista from segnacollo where CODICE_COLLO = '" + codiceCollo + "' and COD_ARTICOLO = '" + codArticolo + "';")){
					if(rs1.next()){
						if(Integer.parseInt(qtaResa) > rs1.getInt("qta_prevista")){
							throw new RuntimeException("non puoi rendere più articoli di quelli ricevuti");
						}else if(Integer.parseInt(qtaResa)<0){
							throw new RuntimeException("non puoi rendere un numero negativo di articoli");
						}else{
							stmt.executeUpdate("update segnacollo set QTA_RESA = " + qtaResa + " where CODICE_COLLO = '" + codiceCollo + "' and COD_ARTICOLO = '" + codArticolo + "'");
							try (ResultSet rs2 = stmt.executeQuery("select QTA_RESA, QTA_PREVISTA, CODICE_COLLO, COD_ARTICOLO, SERIAL, COD_TIPO_RESO from segnacollo s where CODICE_COLLO = '" + codiceCollo + "' and COD_ARTICOLO = '" + codArticolo + "';")){
								body = selectToString(rs2, "<h1>record aggiornato:</h1><br>");
							}
						}
					}else{
						throw new RuntimeException("il colle inserito non esiste");
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}catch (RuntimeException e){
			body = e.getMessage();
		}
		return body;
	}
	
	public static void setPluto(String output) {
		pluto = output;
	}
	
	public static String getPluto() {
		return pluto;
	}
	
	public static String pluto = "";

	public static String getTable() {
		return table;
	}

	public static void setTable(String output){
		DemoApplication.table = output;
	}

	public static String table = "";

	public static String getHome() {
		return home;
	}

	public static void setHome(String output) {
		DemoApplication.home = output;
	}

	public static String home = "";
	
	public static String[] addRow(String[] oldTabel, String row) {
		String[] newTabel = new String[oldTabel.length + 1];
        System.arraycopy(oldTabel, 0, newTabel, 0, oldTabel.length);
		newTabel[oldTabel.length] = row;
		return newTabel;
	}

	@RequestMapping("/textBox")
	public ModelAndView textBox(){
		ModelAndView mav = new ModelAndView("TextBox");
		return mav;
	}

	@GetMapping("/textBox/")
	public String ResultTextBox(@RequestParam String column, @RequestParam String table){
		String body;
		column = column.equals("") ? "*" : column;
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.8.170:3306/testresi", "testresi", "Sip3R§si")) {
            try (Statement stmt = conn.createStatement()) {
                try (ResultSet rs = stmt.executeQuery("SELECT " + column + " from " + table)) {
                	body = selectToString(rs, "");
                }
            }
		} catch (SQLException e) {
            throw new RuntimeException(e);
        }
		return body;
	}

	public static void main(String[] args){
		SpringApplication.run(DemoApplication.class, args);
		String[] nomeTabelle = new String[0];
		String home = "";
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.8.170:3306/testresi", "testresi", "Sip3R§si")) {
			try (Statement stmt = conn.createStatement()) {
				try (ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA='testresi'")) {
					while(rs.next()){
						nomeTabelle = addRow(nomeTabelle, rs.getString(1));
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		home += """
					a che pagina vuoi andare?
						<ul>
							<li>tabelle:
								<ul>
								""";
		for(String nome:nomeTabelle){
			home = home.concat("<li><a href=\"table=" + nome + "\">" + nome + "</a></li>");
		}
		home = home.concat("""
								</ul>
							</li>
							<li>esercizi:
								<ol>
									<li>
										<a href="cod_articolo=88002074">
											Dato un COD_ARTICOLO ritornare la descrizione
										</a>
										<br>
										[il valore di input di default è 88002074]
									</li>
									<li>
										<a href="codice_collo=080538311">
											Dato un CODICE_COLLO verificare se esiste nella tabella segnacollo e ritornare il tipo reso (codice e descrizione)
										</a>
										<br>
										[il valore di input di default è 080538311]
									</li>
									<li>
										<a href="codice_collo=080538311/cod_articolo=88002088/serial=lIWEUGFWEIUGF">
											In ingresso codice collo, codice articolo e una matricola da salvare in SERIAL  (ritorna un esito 1/0)
										</a>
										<br>
										[valori di default: codice_collo: 080538311, cod_articolo: 88002088, serial: lIWEUGFWEIUGF]
									</li>
									<li>
										<a href="codice_collo=080538311/cod_articolo=88002088/qta_resa=3">
											In ingresso codice collo, codice articolo e una quantità intera, che deve essere minore o uguale a QTA_PREVISTA e salvarla in QTA_RESA (ritorna un esito 1/0)
										</a>
										<br>
										[valori di default: codice_collo: 080538311, cod_articolo: 88002088, Qta_resa: 3]
										<br>
									</li>
								</ol>
							</li>
							<li>
								<ul>
									<li>
										<a href="textBox">
											UI che consente di fare il select inserendo colonna e tabella
										</a>
									</li>
								</ul>
							</li>
						</ul>
						
						todo:
						<ul>
							
						</ul>
				""");
		setHome(home);

    }
}
