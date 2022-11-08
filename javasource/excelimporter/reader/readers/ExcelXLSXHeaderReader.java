package excelimporter.reader.readers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRelation;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import replication.ValueParser.ParseException;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive.PrimitiveType;

import excelimporter.reader.readers.ExcelRowProcessor.ExcelCellData;
import excelimporter.reader.readers.replication.ExcelValueParser;

public class ExcelXLSXHeaderReader extends ExcelXLSXReader implements ExcelHeadable {

	public static ILogNode logNode = Core.getLogger("ExcelXLSXHeaderReader");
	private List<ExcelColumn> excelColumns;

	public ExcelXLSXHeaderReader( String fullPathExcelFile, int sheetNr, int rowNr, int columnCount ) throws CoreException, IOException, SAXException, OpenXML4JException {
		OPCPackage opcPackage = null;
		InputStream sheet = null;
		try {
			this.excelColumns = new ArrayList<ExcelColumn>();
			opcPackage = OPCPackage.open(fullPathExcelFile, PackageAccess.READ);
			XSSFReader reader;
			try {
				reader = new XSSFReader(opcPackage);
			}
			catch( NullPointerException e ) {
				throw new CoreException("Invalid excel file structure, validate your document", e);
			}
			ReadOnlySharedStringsTable stringsTable = new ReadOnlySharedStringsTable(opcPackage);
			StylesTable stylesTable = reader.getStylesTable();

			XMLReader parser = XMLReaderFactory.createXMLReader();
			ExcelXLSXReader.setXMLReaderProperties(parser);
			logNode.trace("Loaded SAX Parser: " + parser);
			SheetHandler handler = new SheetHandler(stringsTable, stylesTable, rowNr + 1, sheetNr, columnCount);
			parser.setContentHandler(handler);

            ArrayList<PackagePart> sheets = opcPackage.getPartsByContentType(XSSFRelation.WORKSHEET.getContentType());
            sheet = sheets.get(sheetNr).getInputStream();
			InputSource sheetSource = new InputSource(sheet);
			parser.parse(sheetSource);
		}
		finally {
			if ( sheet != null )
				sheet.close();
			if ( opcPackage != null )
				opcPackage.revert();
		}
	}

	@Override
	public List<ExcelColumn> getColumns() {
		return this.excelColumns;
	}

	/**
	 * See org.xml.sax.helpers.DefaultHandler javadocs
	 */
	private class SheetHandler extends ExcelSheetHandler {

		private boolean isProcessingHeaderRow = false;
		private int headerRowNr = -1;
		private ExcelValueParser valueParser;

		private SheetHandler( ReadOnlySharedStringsTable stringsTable, StylesTable stylesTable, int rowNr, int sheetNr, int columnCount ) {
			super(stringsTable, stylesTable, sheetNr, rowNr, columnCount);

			this.headerRowNr = rowNr;
			this.valueParser = new ExcelValueParser(null, null);
		}

		@Override
		public void startElement( String uri, String localName, String name, Attributes attributes ) throws SAXException {

			if ( evaluateTextTag(localName, true) ) {
			}
			else if ( evaluateColumn(localName, attributes) ) {
				evaluateCellStyle(attributes);
			}
			else if ( evaluateFormula(name) ) {
			}
			else if ( evaluateRow(localName, attributes) ) {
				if ( this.getCurrentRow() == this.headerRowNr )
					this.isProcessingHeaderRow = true;
				else
					this.isProcessingHeaderRow = false;
			}
		}

		@Override
		public void endElement( String uri, String localName, String name ) throws SAXException {

			// If there is something wrong with the Excel XML then we don't try to fix it here.
			if ( this.isProcessingHeaderRow && this.shouldHandleRow() ) {
				closeTextProcessing(localName, true);

				if ( localName.equals("row") ) {
					ExcelCellData[] values = this.getValues();
					for( int i = 0; i < values.length; i++ ) {
						String alias = String.valueOf(i);
						String processedValue;
						try {
							processedValue = (String) this.valueParser.getValueFromDataSet(alias, PrimitiveType.String, values);
						}
						catch( ParseException e ) {
							throw new SAXException("Unable to process Excel Header value. Error found in row: " + this.getCurrentRow() + " column: " + values[i].getColNr(), e);
						}

						if ( values[i] != null )
							ExcelXLSXHeaderReader.this.excelColumns.add(new ExcelColumn(values[i].getColNr(), processedValue));
					}
				}
			}
		}
	}


}
