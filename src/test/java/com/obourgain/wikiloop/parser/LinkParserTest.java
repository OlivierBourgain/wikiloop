package com.obourgain.wikiloop.parser;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * 
 */
public class LinkParserTest {
    private static final String TITLE = "Page 0";
	static Map<String, Long> dict = new HashMap<String, Long>();
    static LinkParser fixture;

    static {
        dict.put("Page1", 1L);
        dict.put("Page2", 2L);
        dict.put("Page3", 3L);
        dict.put("Page4", 4L);
        dict.put("Internationale situationniste", 10L);
        dict.put("Rouen", 11L);
        dict.put("Personne morale en droit français", 12L);
        fixture = new LinkParser(dict);
    }


    @Test
    public void simpleLink() throws Exception {
        String text = "[[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }


    @Test
    public void simpleLinkWithText() throws Exception {
        String text = "[[Page1|Lien vers la page 1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void deuxLiens() throws Exception {
        String text = "[[Page1|Lien vers la page 1]] [[Page2]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void ignoreLienRouge() throws Exception {
        String text = "[[PageX|Lien rouge]] [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }


    @Test
    public void ignoreTemplate() throws Exception {
        String text = "{{Template}} [[Page2]] [[Page1]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void ignoreLinkInTemplate() throws Exception {
        String text = "{{Template [[Page3]] }} [[Page2]] [[Page1]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void ignoreImage() throws Exception {
        String text = "[[Image:image.png|data|data]] [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void ignoreItalicText() throws Exception {
        String text = "''Texte en italique'' [[Page3]]";
        Assert.assertEquals(Long.valueOf(3L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void ignoreLinkInItalic() throws Exception {
        String text = "''[[Page2]]'' [[Page3]]";
        Assert.assertEquals(Long.valueOf(3L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void linkInBold() throws Exception {
        String text = "'''[[Page2]]''' [[Page3]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void linkAfterBold() throws Exception {
        String text = "'''Text in bold''' [[Page3]]";
        Assert.assertEquals(Long.valueOf(3L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void linkWithDifferentCase() throws Exception {
        String text = " [[page3]]";
        Assert.assertEquals(Long.valueOf(3L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void linkInImage() {
        String text = "[[Image:x|x|[[Page1]] a [[Page2]]]] [[Page3]]";
        Assert.assertEquals(Long.valueOf(3L), fixture.getFirstLink(TITLE, text));
    }


    @Test
    public void linkInFichier() {
        String text = "[[Fichier:x|x|[[Page1]] a [[Page2]]]] [[Page3]]";
        Assert.assertEquals(Long.valueOf(3L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void infoboxInInfobox() {
        String text = "{{Infobox1 {{Infobox2}} [[Page1]]  }} [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }


    @Test
    public void linkWithAnchor() {
        String text = "[[Page1#Construction]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void linkWithOnlyAnchor() {
        String text = "[[#Construction]] [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void ignoreRef() {
        String text = "Text <ref>[[Page1]] </ref> [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void ignoreRefWithGroup() {
        String text = "Text <ref group=note>[[Page1]] </ref> [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void test1() {
        String text = "#REDIRECT [[Internationale situationniste]]";
        Assert.assertEquals(Long.valueOf(10L), fixture.getFirstLink(TITLE, text));

    }

    @Test
    public void uncapitaliseLink() {
        String text = "xx [[page4]] [[Page2]]";
        Assert.assertEquals(Long.valueOf(4L), fixture.getFirstLink(TITLE, text));

    }

    @Test
    public void ignoreBoldAndItalic() {
        String text = "'''''Le Berceau du chat''''' (titre original en anglais : ''Cat's Cradle'') est un [[page1|text]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void ignoreLinkInBoldAndItalic() {
        String text = "'''''[[Page2]]'''''  [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void templateSuiviDunTitre() {
        String text = "{{Titre en italique}}''' Rendez-vous à Bray''' est un [[Page1|film]] franco-belge d'[[André Delvaux]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void test4() {
        String text = "La '''Sud-Aviation SE 210 ''Caravelle''''' est un [[page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void test5() {
        String text = "L''''allemand de Pennsylvanie''' [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void test7() {
        String text = "Le '''''brocciu''''' ou '''''brucciu''''' ou encore '''''brocciu'' corse''' [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void test9() {
        String text = "'' '''Les Cracks''' '' est un [[page1|film]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));

    }

    @Test
    public void boldInsideItalic() {
        String text = "''('''A''')'' [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));

    }


    @Test
    public void nullLink() {
        String text = "[[]] [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));

    }

    @Test
    public void blankLink() {
        String text = "[[  ]] [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));

    }


    @Test
    public void testLanguesIndoEuropeennes() {
        String text = "[[File:x|y {{legend|z}} a [[Page1]] b]] [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testRefNecessaire() {
        String text = "{{refnec| [[Page1]] }} [[Page3]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testRefSouhaitee() {
        String text = "{{refsou| [[Page1]] }} [[Page3]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testEtymologie() {
        // Il ne faut pas prendre en compte ce modèle, qui insère des () autour du texte.
        String text = "La '''science''' {{étymologie|latin|scientia|[[page1]]}} [[Page2]] ";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testCitation() {
        String text = "est {{Citation|ce que l'on sait pour l'avoir appris, ce que l'on tient pour [[Page2]] au sens large }} [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testParenthese() {
        String text = "La '''démocratie''' (du [[Page1]] ) [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testNestedParenthese() {
        String text = "a (b [[Page1]] ( [[Page3]] ) ) [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testApostropheSuiviItalic() {
        // from http://fr.wikipedia.org/wiki/Angerville
        String text = "domaine d'''Asgeir'' [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testApostropheSuiviItalic2() {
        // from http://fr.wikipedia.org/wiki/Angerville
        String text = "xxx '''yyyd'''. [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testEncyclopedie() {
        // from https://fr.wikipedia.org/wiki/Encyclop%C3%A9die
        String text = "Dès sa parution en 1968, l'''[[Page2]]'' est devenue une référence [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }


    @Test
    public void testScience() {
        String text = "La '''science''' {{étymologie|latin|scientia|[[Page1]]}} est {{Citation|ce que l'on sait pour l'avoir appris, ce que l'on tient pour [[Page2]] au sens large";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testAnnee150() {
        // Page 150
        String text = "''Cette page concerne l'année '''150''' du [[Page1]]'' [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testNobr() {
        // Par exemple "Légendaire de Tolkien"
        String text = "{{nobr|[[Page1]]}} [[Page2]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }


    @Test
    public void testLienEnCommentaire() {
        String text = "<!-- Ignorer ce lien [[Page2]] --> [[Page3]]";
        Assert.assertEquals(Long.valueOf(3L), fixture.getFirstLink(TITLE, text));

    }

    @Test
    public void testRedirect() {
        String text = "#REDIRECT[[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testRedirectWithUnderscore() {
        String text = "#REDIRECT[[Internationale_situationniste]]";
        Assert.assertEquals(Long.valueOf(10L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testPersonneMorale() {
        String text = "#REDIRECT[[Personne_morale_en_droit_fran%C3%A7ais#Personnes_morales_de_droit_public]]";
        Assert.assertEquals(Long.valueOf(12L), fixture.getFirstLink(TITLE, text));

    }

    @Test
    public void testStEtienne() {
        String text = "'''Saint-Étienne''' ([[Aide:Alphabet phonétique international|prononcé]] {{MSAPI|s|ɛ̃|.|t|‿|e|.|ˈ|t|j|ɛ|n}}) est une ville de [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testKronecker() {
        // from : http://fr.wikipedia.org/wiki/Symbole_de_Kronecker
        String text = "'''''N.B.:''' xxx [[Page1]] xxx '''[[Page2]]''' xx'' [[Page3]]";
        Assert.assertEquals(Long.valueOf(3L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void test1933() {
        String text = "''Cette Page concerne l'année '''1933''' du [[Page1]].'' [[Page2]]";
        Assert.assertEquals(Long.valueOf(2L), fixture.getFirstLink(TITLE, text));
    }

    @Test
    public void testPierreAntoineMotteux() {
        String text = "    {à illustrer}}\n'''Pierre-Antoine Motteux'''<ref>Ou quelquefois Le Motteux.</ref >, né à [[Rouen]] le [[25 février]] [[1663]] et mort le [[18 février]] [[1718]] à [[Londres]], fut un [[dramaturge]] et [[Traduction|traducteur]] [[Normands|normand]].";
        Assert.assertEquals(Long.valueOf(11L), fixture.getFirstLink(TITLE, text));
    }
    
    @Test
    public void testControleAerien() {
    	String text = "'''contrôle du trafic aérien''' (en anglais ''Air Traffic Control'' ou '''''ATC'''''), "
    				+ " ou '''contrôle de la circulation aérienne''', ou également appelé '''contrôle aérien'''," 
    			    + " est l'un des trois types de [[Page1]].";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }

    
    @Test
    public void testLinkAfterImage() {
    	String text = "[[Image:x]][[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }
    
    @Test
    public void testLinkAfterFichier() {
    	String text = "[[Fichier:x]][[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }
    
    @Test
    public void testLinkAfterFichier2() {
    	String text = "[[Fichier:x[[Image:y]]]][[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));
    }
    
    @Test
    public void testApostropheItaliqueGras(){
    	String text = "L''''''X''''' est [[Page1]]";
        Assert.assertEquals(Long.valueOf(1L), fixture.getFirstLink(TITLE, text));

    }
    
    

}