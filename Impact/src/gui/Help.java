/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOUSE. See the GNU
 * General Public License for more details.
 *
 * You should have recieved a copy of the GNU General Public License
 * along with this program; if not write to the Free Software
 * Foundation, inc., 59 Temple Place, Suite 330, Boston MA 02111-1307
 * USA
 */

package gui;

import javax.swing.*;
import java.awt.*;
import java.net.URI;

import javax.swing.event.*;
import javax.swing.text.html.*;

/**
 * Insert the type's description here.
 * 
 * @author: Yuriy Mikhaylovskiy
 */

public class Help extends JPanel {
	public static final String ver = "Help";
	BorderLayout borderLayout1 = new BorderLayout();
	JScrollPane jScrollPane1 = new JScrollPane();
	JEditorPane EditorPane = new JEditorPane();

	public Help() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		EditorPane
				.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						EditorPane_hyperlinkUpdate(e);
					}
				});
		this.add(jScrollPane1, BorderLayout.CENTER);
		jScrollPane1.getViewport().add(EditorPane, null);
		EditorPane.setPage(Help.class.getClassLoader().getResource(
				"index_us.html"));
		EditorPane.setEditable(false);
	}

	void EditorPane_hyperlinkUpdate(HyperlinkEvent e) {
		try {
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				JEditorPane pane = (JEditorPane) e.getSource();
				if (e instanceof HTMLFrameHyperlinkEvent) {
					HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) e;
					HTMLDocument doc = (HTMLDocument) pane.getDocument();
					if ((evt.getTarget() + "").indexOf("_BLANK") != -1)
						try {
							Desktop.getDesktop().browse(evt.getURL().toURI());
						} catch (Exception e1) {
						}
					else if ((evt.getURL() + "").indexOf("TableOfContents") != -1) {
						doc.processHTMLFrameHyperlinkEvent(new HTMLFrameHyperlinkEvent(
								e.getSource(),
								HyperlinkEvent.EventType.ACTIVATED, evt
										.getURL(), "menu"));
					} else
						doc.processHTMLFrameHyperlinkEvent(new HTMLFrameHyperlinkEvent(
								e.getSource(),
								HyperlinkEvent.EventType.ACTIVATED, evt
										.getURL(), "main"));
				} else
					pane.setPage(e.getURL());
			}
		} catch (Exception t) {
			error(t);
		}
	}

	public void error(Object st) {
		JOptionPane.showMessageDialog(null, "Error: " + st, "Error!",
				JOptionPane.ERROR_MESSAGE);
		System.out.println("Error: " + st);
	}
}