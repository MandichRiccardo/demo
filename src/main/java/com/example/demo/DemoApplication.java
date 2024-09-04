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
		return table;
	}

	@GetMapping("/id={idArticolo}")
	public String idArticolo(@PathVariable("idArticolo") String idArticolo){
		return "<ul><li>id articolo:\t" + idArticolo + "</li></ul>";
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
                	String[] tabel = new String[0];
                	String label = "";
                	for(int i=1;i<=rs.getMetaData().getColumnCount();i++) {
                		label = label.concat("<td>" + rs.getMetaData().getColumnName(i) + "</td>");
                	}
                	tabel = addRow(tabel, label);
                    while(rs.next()) {
	                    String row = "";
	                    for(int i=1;i<=rs.getMetaData().getColumnCount();i++) {
	                    	row = row.concat("<td>" + rs.getString(i) + "</td>");
	                    }
	                    tabel = addRow(tabel, row);
                    }
                    setTable("<table border=\"\">");
                    for(String line:tabel) {
                    	setTable(getTable() + "<tr>" + line + "</tr>");
						/*String[] riga = line.split("</tr><tr>");
						for(String cell:riga){
							System.out.print(cell + "\t");
						}
						System.out.println();*/
                    }
                    setPluto(getTable() + "</table>");
                }
            }
		} catch (SQLException e) {
            throw new RuntimeException(e);
        }
		return getTable();
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
		return table;
	}

	public static void setHome(String output) {
		DemoApplication.table = output;
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
									</li>
									<li>
										<a href="codice_collo=080538311">
											Dato un CODICE_COLLO verificare se esiste nella tabella segnacollo e ritornare il tipo reso (codice e descrizione)
										</a>
									</li>
								</ol>
							</li>
						</ul>
				""");
		System.out.println(getPluto());
    }
}
