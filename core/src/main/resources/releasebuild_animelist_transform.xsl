<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  <xsl:template match="/">
    <html>
      <head>
        <title>Animelist</title>
        <link rel="stylesheet" type="text/css" href="{{STYLESHEET-FILE-PATH}}"/>
      </head>
      <body>
        <table>
          <thead>
            <tr>
              <th>#</th>
              <th>Anime Title</th>
              <th>Infolink</th>
              <th>Type</th>
              <th>Episodes</th>
            </tr>
          </thead>
          <tfoot>
            <tr>
              <td colspan="5">Edit your profile <a href="http://myanimelist.net">here</a>.</td>
            </tr>
          </tfoot>
          <tbody>
            <xsl:apply-templates/>
          </tbody>
        </table>
      </body>
    </html>
  </xsl:template>
  <xsl:template match="animeList">
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="anime">
    <xsl:variable name="counter">
      <xsl:number level="any" count="anime"/>
    </xsl:variable>
    <xsl:element name="tr">
      <xsl:element name="td">
        <xsl:attribute name="class">centered</xsl:attribute>
        <xsl:value-of select="$counter"/>
      </xsl:element>
      <xsl:element name="td">
        <xsl:element name="a">
          <xsl:attribute name="href">
            <xsl:value-of select="@location"/>
          </xsl:attribute>
          <xsl:value-of select="@title"/>
        </xsl:element>
      </xsl:element>
      <xsl:element name="td">
        <xsl:attribute name="class">centered</xsl:attribute>
        <xsl:if test="@infoLink!=' '">
          <xsl:element name="a">
            <xsl:attribute name="href">
              <xsl:value-of select="@infoLink"/>
            </xsl:attribute>
            <xsl:attribute name="target">_blank</xsl:attribute>
info
          </xsl:element>
        </xsl:if>
      </xsl:element>
      <xsl:element name="td">
        <xsl:attribute name="class">centered</xsl:attribute>
        <xsl:value-of select="@type"/>
      </xsl:element>
      <xsl:element name="td">
        <xsl:attribute name="class">centered</xsl:attribute>
        <xsl:value-of select="@episodes"/>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="watchList">
    <xsl:param name="hideWatchList"><xsl:apply-templates/></xsl:param>
    <input type="hidden" value="{$hideWatchList}" />    
  </xsl:template>

  <xsl:template match="filterList">
    <xsl:param name="hideFilterList"><xsl:apply-templates/></xsl:param>
    <input type="hidden" value="{$hideFilterList}" />    
  </xsl:template>
</xsl:stylesheet>