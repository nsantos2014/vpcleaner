<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:svg="http://www.w3.org/2000/svg">
	<xsl:output method="xml" indent="yes"
  doctype-public="-//W3C//DTD SVG 1.0//EN"
  doctype-system="http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd"
	/>

	<xsl:template match="svg:g[contains(svg:text/text(),'[evaluation copy]')]">        
    </xsl:template>

    <xsl:template match="node()">
        <xsl:copy>
        	<xsl:copy-of select="@*"/>
            <xsl:apply-templates select="*|text()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="text()">
        <xsl:copy-of select="."/>
    </xsl:template>	
</xsl:stylesheet>