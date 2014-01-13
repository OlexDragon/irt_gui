package irt.tools.panel.ip_address;

import irt.controller.text.document.DocumentsFactory;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.FocusManager;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;


public class IpAddressTextField extends GridbagPanel {

	private final Logger logger = (Logger) LogManager.getLogger();

	private static final long serialVersionUID = -4163874955176718452L;
		/**
		 * a text fields for each byte
		 */
		private JTextField[] textFields;

		/**
		 * dots between text fields
		 */
		private JLabel[] dotsLabels;


		/**
		 * used to calculate enable/disable color; never shown
		 */
		private static JTextField sampleTextField = new JTextField();

		/**
		 * listen to changes in the byte fields
		 */
		private MyDocumentListener documentListener;

		/**
		 * list of key listeners
		 */
		private List<KeyListener> keyListenersList;

		/**
		 * List of Focus Adapter that select all data in JTextFiled during action
		 * */
//		private List<FocusAdapter> focusAdapterList;

		/**
		 * list of key listeners
		 */
		private List<FocusListener> focusListenersList;

		private int maxHeight = 0;

		/**
		 * @param byteCount
		 *            number of bytes to display
		 */
		public IpAddressTextField() {

		    textFields = new JTextField[4];
		    for (int i = 0; i < textFields.length; i++) {
		        textFields[i] = new JTextField(3);

		    }

		    List<JLabel> dotsLabelsList = new ArrayList<JLabel>();

		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        textField.setHorizontalAlignment(JTextField.CENTER);
		        Document document = DocumentsFactory.createIntDocument(255);
		        textField.setDocument(document);

		        if (i < textFields.length-1) {
		            add(textField, i * 2, 0, 1, 1);
		            if (textField.getPreferredSize().height > maxHeight)
		                maxHeight = textField.getPreferredSize().height;
		            JLabel label = new JLabel(".");



		            add(label, (i * 2) + 1, 0, 1, 1);
		            if (label.getPreferredSize().height > maxHeight)
		                maxHeight = label.getPreferredSize().height;
		            dotsLabelsList.add(label);
		        } else
		            add(textField, i * 2, 0, 1, 1);

		    }

		    //dotsLabels = new JLabel[dotsLabelsList.size()];
		    dotsLabels = new JLabel[dotsLabelsList.size()];


		    dotsLabels = dotsLabelsList.toArray(dotsLabels);

		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        textField.setBorder(BorderFactory.createEmptyBorder());
		    }

		    //init
		    Color backgroundColor = UIManager.getColor("TextField.background");
		    setBackground(backgroundColor);
		    Border border = UIManager.getBorder("TextField.border");
		    setBorder(border);

		    //register listeners
		    for (int i = 1; i < textFields.length; i++) {
		        JTextField field = textFields[i];
		        field.addKeyListener(new BackKeyAdapter());
		    }

		    documentListener = new MyDocumentListener();
		    for (int i = 0; i < textFields.length; i++) {
		        JTextField field = textFields[i];
		        field.getDocument().addDocumentListener(documentListener);
		        field.addKeyListener(new ForwardKeyAdapter());
		    }

		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        textField.addKeyListener(new MyKeyListener());
		    }

		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        textField.addFocusListener(new MyFocusAdapter());
		    }

		    keyListenersList = new ArrayList<KeyListener>();
		    focusListenersList = new ArrayList<FocusListener>();
//		    focusAdapterList = new ArrayList<FocusAdapter>();
		}

		public synchronized void addKeyListener(KeyListener l) {
		    super.addKeyListener(l);
		    keyListenersList.add(l);
		}

		public synchronized void addFocusListener(FocusListener l) {
		    super.addFocusListener(l);
		    if (focusListenersList != null)
		        focusListenersList.add(l);
		}

		public synchronized void removeKeyListener(KeyListener l) {
		    super.removeKeyListener(l);
		    if (focusListenersList != null)
		        keyListenersList.remove(l);
		}

		public synchronized void removeFocusListener(FocusListener l) {
		    super.removeFocusListener(l);
		    keyListenersList.remove(l);
		}

		public void setEnabled(boolean b) {
		    super.setEnabled(b);
		    sampleTextField.setEnabled(b);
		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        textField.setEnabled(b);
		    }

		    for (int i = 0; i < dotsLabels.length; i++) {
		        JLabel dotsLabel = dotsLabels[i];           
		        dotsLabel.setEnabled(b);
		    }

		    setBackground(sampleTextField.getBackground());
		    setForeground(sampleTextField.getForeground());
		    setBorder(sampleTextField.getBorder());

		}

		public void requestFocus() {
		    super.requestFocus();
		    textFields[0].requestFocus();
		}

		public void setEditable(boolean b) {
		    sampleTextField.setEditable(b);
		    setBackground(sampleTextField.getBackground());
		    setForeground(sampleTextField.getForeground());
		    setBorder(sampleTextField.getBorder());
		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        textField.setEditable(b);
		    }

		    for (int i = 0; i < dotsLabels.length; i++) {
		        JLabel dotsLabel = dotsLabels[i];

		        dotsLabel.setForeground(sampleTextField.getForeground());
		    }
		}

		public boolean isFieldEmpty() {
		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        String sCell = textField.getText().trim();
		        if (!(sCell.equals("")))
		            return false;
		    }
		    return true;
		}

	@Override
	public Dimension getPreferredSize() {
		if (super.getPreferredSize().height > maxHeight)
			maxHeight = super.getPreferredSize().height;
		return new Dimension(super.getPreferredSize().width, maxHeight);
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	/**
	 * clears current text in text fiekd
	 */
	private void reset() {
		for (int i = 0; i < textFields.length; i++) {
			JTextField textField = textFields[i];
			textField.getDocument().removeDocumentListener(documentListener);
			textField.setText("");
			textField.getDocument().addDocumentListener(documentListener);
		}
	}

		public void setText(String version) {
		    if (version == null || "".equals(version) || "null".equals(version))
		        reset();
		    else {           
		        setVer(version.split("[.]"));
		    }
		}


		private void setVer(String[] ver) {
		    if (ver == null) {
		        reset();
		        return;
		    }

		    Enumeration<String> enumeration =  Collections.enumeration(Arrays.asList(ver));
		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        String s = (String) enumeration.nextElement();
		        textField.getDocument().removeDocumentListener(documentListener);
		        textField.setText(s);
		        textField.getDocument().addDocumentListener(documentListener);
		    }
		}

		public void setToolTipText(String toolTipText) {
		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        textField.setToolTipText(toolTipText);
		    }
		}

		private class MyDocumentListener implements DocumentListener {

		    @Override
		    public void insertUpdate(DocumentEvent e) {
		        Document document = e.getDocument();
		        try {
		            JTextField textField = (JTextField) FocusManager.getCurrentManager().getFocusOwner();

		            String s = document.getText(0, document.getLength());

		            if (s.length() == 4){ // && textField.getCaretPosition() == 2) {
		                textField.transferFocus();


		            }

		        } catch (BadLocationException e1) {
		            logger.catching(e1);;
		            return;
		        }

		    }

		    public void removeUpdate(DocumentEvent e) {
		    }

		    @Override
		    public void changedUpdate(DocumentEvent e) {
		        //          Document document = e.getDocument();
		        //          try {
		        //              Component component = FocusManager.getCurrentManager().getFocusOwner();
		        //              String s = document.getText(0, document.getLength());
		        //              
		        //              // get selected integer
		        //              int valueInt = Integer.parseInt(s);
		        //              
		        //              if (valueInt > 25) {
		        //                  component.transferFocus();
		        //              }
		        //
		        //          } catch (BadLocationException e1) {
		        //              e1.printStackTrace();
		        //              return;
		        //          }
		    }
		}

		private class BackKeyAdapter extends KeyAdapter {

		    public void keyPressed(KeyEvent e) {
		        JTextField textField = (JTextField) e.getComponent();
		        if (textField.getCaretPosition() == 0
		                && KeyEvent.VK_LEFT == e.getKeyCode()
		                && e.getModifiers() == 0)
		            textField.transferFocusBackward();
		        if (textField.getCaretPosition() == 0
		                && KeyEvent.VK_BACK_SPACE == e.getKeyCode()
		                && e.getModifiers() == 0) {
		            textField.transferFocusBackward();
		        }
		    }
		}

		private class ForwardKeyAdapter extends KeyAdapter {
		    public void keyPressed(KeyEvent e) {
				logger.debug("keyPressed({})", e);

				JTextField textField = (JTextField) e.getComponent();

		        int keyCode = e.getKeyCode();
				boolean noModifiers = e.getModifiers() == 0;
				if(KeyEvent.VK_ENTER == keyCode && noModifiers){
					logger.debug("textFields.length={}", textFields.length);
					textField.transferFocus();
		            e.consume();
				}else if (KeyEvent.VK_RIGHT == keyCode && noModifiers) {
		            int length = textField.getText().length();
		            int caretPosition = textField.getCaretPosition();

		            if (caretPosition == length) {
		                textField.transferFocus();
		                e.consume();
		            }
		        }else if (e.getKeyChar() == '.' && textField.getText().trim().length() != 0) {
		            textField.setText(textField.getText().trim());
		            textField.transferFocus();
		            e.consume();
		        }
		    }
		}

		/**
		 * @return current text in ip text field
		 */
		public String getText()  {
		    StringBuffer buffer = new StringBuffer();
		    String ipResult;
		    for (int i = 0; i < textFields.length; i++) {
		        JTextField textField = textFields[i];
		        logger.trace("textField.getText()={}", textField.getText());

		        if(textField.getText().trim().equals("")){
		        	textField.setText("0");
		        }

		        buffer.append(Integer.parseInt(textField.getText()));
		        if (i < textFields.length - 1){
		            buffer.append('.');
		        }
		    }
		    ipResult = buffer.toString();       

		    return ipResult;
		}

		/**
		 * general purpose key listener
		 */
	private class MyKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			for (int i = 0; i < keyListenersList.size(); i++) {
				keyListenersList.get(i).keyPressed(

						new KeyEvent(
								IpAddressTextField.this,
								e.getID(),
								e.getWhen(),
								e.getModifiers(),
								e.getKeyCode(),
								e.getKeyChar(),
								e.getKeyLocation()));
			}
		}

		public void keyReleased(KeyEvent e) {
			for (int i = 0; i < keyListenersList.size(); i++) {
				KeyListener keyListener = keyListenersList.get(i);
				keyListener.keyReleased(
						new KeyEvent(
								IpAddressTextField.this,
								e.getID(),
								e.getWhen(),
								e.getModifiers(),
								e.getKeyCode(),
								e.getKeyChar(),
								e.getKeyLocation()));
			}
		}

		public void keyTyped(KeyEvent e) {
			for (int i = 0; i < keyListenersList.size(); i++) {
				KeyListener keyListener = keyListenersList.get(i);
				keyListener.keyTyped(
						new KeyEvent(
								IpAddressTextField.this,
								e.getID(),
								e.getWhen(),
								e.getModifiers(),
								e.getKeyCode(),
								e.getKeyChar(),
								e.getKeyLocation()));
			}
		}
	}

	private class MyFocusAdapter extends FocusAdapter {

		public void focusGained(FocusEvent e) {
			for (int i = 0; i < focusListenersList.size(); i++) {
				FocusListener focusListener = focusListenersList.get(i);
				focusListener.focusGained(
						new FocusEvent(
								IpAddressTextField.this,
								e.getID(), e.isTemporary(),
								e.getOppositeComponent()));
			}

			if (e.getComponent() instanceof javax.swing.JTextField) {
				highlightText((JTextField) e.getSource());
			}
			logger.debug("MyFocusAdapter.focusGained: text={}", ((JTextField)e.getSource()).getText());
		}

		public void focusLost(FocusEvent e) {
			for (int i = 0; i < focusListenersList.size(); i++) {
				FocusListener focusListener = focusListenersList.get(i);
				focusListener.focusLost(
						new FocusEvent(IpAddressTextField.this,
								e.getID(),
								e.isTemporary(),
								e.getOppositeComponent()));
			}
		}

		public void highlightText(javax.swing.JTextField ctr) {
			// ctr.setSelectionColor(Color.BLUE);
			// ctr.setSelectedTextColor(Color.WHITE);
			// ctr.setSelectionStart(0);
			// ctr.setSelectionEnd(ctr.getText().length());
			ctr.selectAll();
		}
	}

	public static void main(String[] args) {

		JFrame frame = new JFrame("test");
		IpAddressTextField ipTextField = new IpAddressTextField();
		ipTextField.setText("9.1.23.147");
		Container contentPane = frame.getContentPane();
		contentPane.setLayout(new FlowLayout());
		contentPane.add(ipTextField);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public void setDisabledTextColor(Color color) {
		for(JTextField tf:textFields)
			tf.setDisabledTextColor(color);
	}
}
