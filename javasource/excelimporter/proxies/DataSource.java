// This file was generated by Mendix Studio Pro.
//
// WARNING: Code you write here will be lost the next time you deploy the project.

package excelimporter.proxies;

public enum DataSource
{
	CellValue(new java.lang.String[][] { new java.lang.String[] { "en_US", "Cell Value" }, new java.lang.String[] { "en_GB", "Cell Value" }, new java.lang.String[] { "en_ZA", "Cell Value" }, new java.lang.String[] { "nl_NL", "Cel waarde" } }),
	DocumentPropertyRowNr(new java.lang.String[][] { new java.lang.String[] { "en_US", "Document property - row number" }, new java.lang.String[] { "en_GB", "Document property - row number" }, new java.lang.String[] { "en_ZA", "Document property - row number" }, new java.lang.String[] { "nl_NL", "Document eigenschap - rij nummer" } }),
	DocumentPropertySheetNr(new java.lang.String[][] { new java.lang.String[] { "en_US", "Document property - sheet number" }, new java.lang.String[] { "en_GB", "Document property - sheet number" }, new java.lang.String[] { "en_ZA", "Document property - sheet number" }, new java.lang.String[] { "nl_NL", "Document eigenschap - sheet nummer" } }),
	StaticValue(new java.lang.String[][] { new java.lang.String[] { "en_US", "Static Value - Copy caption into the member" }, new java.lang.String[] { "nl_NL", "Statische Waarrde - Kopier de titel in de Member" }, new java.lang.String[] { "en_GB", "Static Value - Copy caption into the member" }, new java.lang.String[] { "en_ZA", "Static Value - Copy caption into the member" } });

	private final java.util.Map<java.lang.String, java.lang.String> captions;

	private DataSource(java.lang.String[][] captionStrings)
	{
		this.captions = new java.util.HashMap<>();
		for (java.lang.String[] captionString : captionStrings) {
			captions.put(captionString[0], captionString[1]);
		}
	}

	public java.lang.String getCaption(java.lang.String languageCode)
	{
		return captions.getOrDefault(languageCode, "en_US");
	}

	public java.lang.String getCaption()
	{
		return captions.get("en_US");
	}
}
