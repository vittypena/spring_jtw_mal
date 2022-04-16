package com.bolsadeideas.springboot.app.view.pdf;

import java.awt.Color;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractPdfView;

import com.bolsadeideas.springboot.app.models.entity.Factura;
import com.bolsadeideas.springboot.app.models.entity.ItemFactura;
import com.lowagie.text.Document;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

@Component("factura/ver")
public class FacturaPdfView extends AbstractPdfView{

	@Override
	protected void buildPdfDocument(Map<String, Object> model, Document document, PdfWriter writer,
			HttpServletRequest request, HttpServletResponse response) throws Exception {
		//Obtenemos la factura que le hemos pasado desde el controlador con Model, aqui se guarda como Object, por lo tanto hacemos un cast.
		Factura factura = (Factura)model.get("factura");
		
		//Hay que replicar lo que teniamos el HTMl, tablas y tal
		PdfPTable tabla = new PdfPTable(1);	//Una sola columna
		tabla.setSpacingAfter(20);	//Dar un espacio
		
		PdfPCell cell = null;	//Para customizar una celda lo hacemos asi, sin añadir con addCell directamente el valor
		cell= new PdfPCell(new Phrase("Datos del Cliente"));
		cell.setBackgroundColor(new Color(184, 218, 255));
		cell.setPadding(8f);	//8f de float
		tabla.addCell(cell);
		
		
		tabla.addCell(factura.getCliente().getNombre() + " " + factura.getCliente().getApellido());
		tabla.addCell(factura.getCliente().getEmail());
		
		PdfPTable tabla2 = new PdfPTable(1);
		tabla2.setSpacingAfter(20);
				
		cell= new PdfPCell(new Phrase("Datos de la Factura"));
		cell.setBackgroundColor(new Color(195, 230, 203));
		cell.setPadding(8f);	//8f de float
		tabla2.addCell(cell);
		
		tabla2.addCell("Folio: " + factura.getId());
		tabla2.addCell("Descripción: " + factura.getDescripcion());
		tabla2.addCell("Fecha: " + factura.getCreateAt());
		
		//Guardar estas tablas en el documento pdf
		document.add(tabla);
		document.add(tabla2);
		
		
		//Header de la 3 tabla
		PdfPTable tabla3 = new PdfPTable(4);	//Manejamos el item
		tabla3.setWidths(new float[] {3.5f, 1, 1, 1});	//Cambiar el ancho
		tabla3.addCell("Producto");
		tabla3.addCell("Precio");
		tabla3.addCell("Cantidad");
		tabla3.addCell("Total");
		
		//Recorremos por cada item, para los campos de la 3 tabla
		for(ItemFactura item: factura.getItems()) {
			tabla3.addCell(item.getProducto().getNombre());
			tabla3.addCell(item.getProducto().getPrecio().toString());
			
			cell = new PdfPCell(new Phrase(item.getCantidad().toString()));
			cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER);
			tabla3.addCell(cell);
			
			tabla3.addCell(item.calcularImporte().toString());
		}
		
		//Footer de la 3ra tabla
		cell = new PdfPCell(new Phrase("Total: "));	//Agregamos nueva celda, le damos un hueco de 3 y lo alinamos a la derecha
		cell.setColspan(3);
		cell.setHorizontalAlignment(PdfPCell.ALIGN_RIGHT);
		tabla3.addCell(cell);
		tabla3.addCell(factura.getTotal().toString());
		
		document.add(tabla3);
	}

}
