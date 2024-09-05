package com.example.demo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.8.170:3306/testresi", "testresi", "Sip3R§si")) {
            // create a Statement
            try (Statement stmt = conn.createStatement()) {
                //execute query
                try (ResultSet rs = stmt.executeQuery("SELECT * from " + table)) {
                //try (ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_TYPE = 'BASE TABLE' AND TABLE_SCHEMA='testresi'")) {
                    //position result to first
                	setTable(selectToString(rs, getTable()));
                }
            }
		} catch (SQLException e) {
            throw new RuntimeException(e);
        }
		return getTable();
    }

	@GetMapping("/codice_collo={codiceCollo}")
	public String codiceCollo(@PathVariable("codiceCollo") String codiceCollo){
		String body = "";
		try (Connection conn = DriverManager.getConnection("jdbc:mysql://10.0.8.170:3306/testresi", "testresi", "Sip3R§si")) {
			try (Statement stmt = conn.createStatement()) {
				try (ResultSet rs1 = stmt.executeQuery("select * from segnacollo where " + codiceCollo)) {
					String codiceTipoResoMagazzino = "";
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

	public static void main(String[] args){
		SpringApplication.run(DemoApplication.class, args);
		setHome("""
					a che pagina vuoi andare?
						<ul>
							<li>tabelle:
								<ul>
									<li>
										<a href="table=ana_articolo">
											ana_articolo
										</a>
									</li>
									<li>
										<a href="table=segnacollo">
											segnacollo
										</a>
									</li>
									<li>
										<a href="table=tab_tipo_reso">
											tab_tipo_reso
										</a>
									</li>
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
										<a href="codice_collo=080538311">
											In ingresso codice collo, articolo e una matricola da salvare in SERIAL  (ritorna un esito 1/0)
										</a>
										<br>
										[valori di default: 080538311, articolo: 88002088, matricola: lIWEUGFWEIUGF]
									</li>
								</ol>
							</li>
						</ul>
						
						todo:
						<ul>
							<li>
								es 2
							</li>
							<li>
								es 3
							</li>
						</ul>
				""");
		System.out.println(getPluto());
    }
}
