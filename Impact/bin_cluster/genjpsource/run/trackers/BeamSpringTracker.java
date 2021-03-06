package run.trackers;
import run.*;

import run.elements.*;

import java.io.*;

public class BeamSpringTracker extends Tracker implements uka.patch.Patchable, uka.transport.Transportable {
   public void createPatch(Object _copy, uka.patch.PatchOutput po) throws java.io.IOException {
      BeamSpringTracker copy = (BeamSpringTracker)_copy;
      super.createPatch(copy, po);
      if (po.writeDiff(this.Component_is_set, copy.Component_is_set)) copy.Component_is_set = this.Component_is_set;
      if (po.writeDiff(this.Target_is_set, copy.Target_is_set)) copy.Target_is_set = this.Target_is_set;
      if (po.writeDiff(this.File_is_set, copy.File_is_set)) copy.File_is_set = this.File_is_set;
      if (po.writeDiff(this.Element_is_set, copy.Element_is_set)) copy.Element_is_set = this.Element_is_set;
      if (po.writeDiff(this.component, copy.component)) copy.component = this.component;
      copy.spring = this.spring = (run.elements.Beam_Spring_2)po.writeDiff(this.spring, copy.spring);
   }
   public void applyPatch(Object _copy, uka.patch.PatchInput pi) throws java.io.IOException, ClassNotFoundException {
      BeamSpringTracker copy = (BeamSpringTracker)_copy;
      super.applyPatch(copy, pi);
      if (pi.hasDiff()) copy.Component_is_set = this.Component_is_set = pi.getDiffAsBoolean();
      if (pi.hasDiff()) copy.Target_is_set = this.Target_is_set = pi.getDiffAsBoolean();
      if (pi.hasDiff()) copy.File_is_set = this.File_is_set = pi.getDiffAsBoolean();
      if (pi.hasDiff()) copy.Element_is_set = this.Element_is_set = pi.getDiffAsBoolean();
      if (pi.hasDiff()) copy.component = this.component = pi.getDiffAsInt();
      if (pi.hasDiff()) copy.spring = this.spring = (run.elements.Beam_Spring_2)pi.getDiffAsObject();
   }
   public void descendReferences(uka.patch.ReferenceConsumer c) throws java.io.IOException {
      super.descendReferences(c);
      c.descend(this.spring);
   }
   public void filterReferences(uka.patch.ReferenceFilter f) {
      super.filterReferences(f);
      this.spring = (run.elements.Beam_Spring_2)f.filter(this.spring);
   }
   public static final uka.transport.TransportDescriptor TRANSPORT_DESCRIPTOR = new uka.transport.TransportDescriptor(){
      public Object unmarshalReference(uka.transport.UnmarshalStream s, int id) throws java.io.IOException, ClassNotFoundException {
         return new BeamSpringTracker(s, id);
      }
      public boolean unmarshal(Object obj, uka.transport.UnmarshalStream s) throws ClassNotFoundException, java.io.IOException {
         ((BeamSpringTracker)obj).unmarshal(s);
         return false;
      }
      public void marshalReference(Object obj, uka.transport.MarshalStream s) throws java.io.IOException {
         ((BeamSpringTracker)obj).marshalReference(s);
      }
      public void marshal(Object obj, uka.transport.MarshalStream s) throws java.io.IOException {
         ((BeamSpringTracker)obj).marshal(s);
      }
      public Object deepClone(Object orig, int id, uka.transport.DeepClone _helper) throws CloneNotSupportedException {
         try {
            return new BeamSpringTracker((BeamSpringTracker)orig, id, _helper);
         }  catch (java.io.IOException ex) {
            throw (CloneNotSupportedException)new CloneNotSupportedException().initCause(ex);
         }
      }
      public boolean deepCloneReferences(Object orig, Object copy, uka.transport.DeepClone _helper) throws CloneNotSupportedException {
         ((BeamSpringTracker)copy).deepCloneReferences((BeamSpringTracker)orig, _helper);
         return false;
      }
      public Class getType() {
         return BeamSpringTracker.class;
      }
   };
   public uka.transport.TransportDescriptor getTransportDescriptor() {
      return TRANSPORT_DESCRIPTOR;
   }
   private static final int _SIZE = uka.transport.BasicIO.SIZEOF_boolean + uka.transport.BasicIO.SIZEOF_boolean + uka.transport.BasicIO.SIZEOF_boolean + uka.transport.BasicIO.SIZEOF_boolean + uka.transport.BasicIO.SIZEOF_int;
   public BeamSpringTracker(uka.transport.UnmarshalStream _stream, int _id) throws java.io.IOException, ClassNotFoundException {
      super(_stream, _id);
   }
   public void unmarshal(uka.transport.UnmarshalStream _stream) throws java.io.IOException, ClassNotFoundException {
      super.unmarshal(_stream);
      _stream.request(BeamSpringTracker._SIZE);
      byte[] _buffer = _stream.getBuffer();
      int _pos = _stream.getPosition();
      this.Component_is_set = uka.transport.BasicIO.extractBoolean(_buffer, _pos);
      _pos += uka.transport.BasicIO.SIZEOF_boolean;
      this.Target_is_set = uka.transport.BasicIO.extractBoolean(_buffer, _pos);
      _pos += uka.transport.BasicIO.SIZEOF_boolean;
      this.File_is_set = uka.transport.BasicIO.extractBoolean(_buffer, _pos);
      _pos += uka.transport.BasicIO.SIZEOF_boolean;
      this.Element_is_set = uka.transport.BasicIO.extractBoolean(_buffer, _pos);
      _pos += uka.transport.BasicIO.SIZEOF_boolean;
      this.component = uka.transport.BasicIO.extractInt(_buffer, _pos);
      _pos += uka.transport.BasicIO.SIZEOF_int;
      _stream.accept(BeamSpringTracker._SIZE);
      this.spring = (run.elements.Beam_Spring_2)_stream.readReference();
   }
   public void marshal(uka.transport.MarshalStream _stream) throws java.io.IOException {
      super.marshal(_stream);
      _stream.reserve(BeamSpringTracker._SIZE);
      byte[] _buffer = _stream.getBuffer();
      int _pos = _stream.getPosition();
      _pos = uka.transport.BasicIO.insert(_buffer, _pos, this.Component_is_set);
      _pos = uka.transport.BasicIO.insert(_buffer, _pos, this.Target_is_set);
      _pos = uka.transport.BasicIO.insert(_buffer, _pos, this.File_is_set);
      _pos = uka.transport.BasicIO.insert(_buffer, _pos, this.Element_is_set);
      _pos = uka.transport.BasicIO.insert(_buffer, _pos, this.component);
      _stream.deliver(_SIZE);
      _stream.writeReference(this.spring);
   }
   public BeamSpringTracker(BeamSpringTracker _orig, int _id, uka.transport.DeepClone _helper) throws CloneNotSupportedException, java.io.IOException {
      super(_orig, _id, _helper);
      this.Component_is_set = _orig.Component_is_set;
      this.Target_is_set = _orig.Target_is_set;
      this.File_is_set = _orig.File_is_set;
      this.Element_is_set = _orig.Element_is_set;
      this.component = _orig.component;
   }
   public void deepCloneReferences(BeamSpringTracker _orig, uka.transport.DeepClone _helper) throws CloneNotSupportedException {
      super.deepCloneReferences(_orig, _helper);
      this.spring = (run.elements.Beam_Spring_2)_helper.internalDeepClone(_orig.spring);
   }
   private static int TYPE = 10;
   private Beam_Spring_2 spring;
   private int component;
   private boolean Element_is_set;
   private boolean File_is_set;
   private boolean Target_is_set;
   private boolean Component_is_set;
   public BeamSpringTracker() {
      super();
      this.component = 0;
   }
   public void collectData() throws IllegalArgumentException {
   }
   private void collectOwnData() throws IllegalArgumentException {
      switch (this.component) {
      case 0: 
         this.result = this.spring.getFx();
         break;
      
      case 1: 
         this.result = this.spring.getFy();
         break;
      
      case 2: 
         this.result = this.spring.getFz();
         break;
      
      case 3: 
         this.result = this.spring.getMx();
         break;
      
      case 4: 
         this.result = this.spring.getMy();
         break;
      
      case 5: 
         this.result = this.spring.getMz();
         break;
      
      default: 
         this.result = 0;
      
      }
   }
   public void calculate() {
   }
   public void parse_Fembic(Token[] param, int lineno, RplVector globalnodelist, RplVector globalelementlist) throws java.text.ParseException {
      int number_of_elements;
      int j;
      Jama.Matrix dummy = new Jama.Matrix(3, 1);
      int i = 0;
      while (i < param.length) {
         if (param[i].getw().toUpperCase().equals("ELEMENT") && param[i + 1].getw().toUpperCase().equals("=")) {
            if (!param[i + 2].getw().toUpperCase().startsWith("[") || !param[i + 2].getw().toUpperCase().endsWith("]")) {
               throw new java.text.ParseException("Error, element number definition should be [elementnr]", lineno);
            }
            try {
               number_of_elements = super.getNumberOfNodes(param[i + 2].getw().toUpperCase());
               if (number_of_elements != 1) {
                  throw new java.text.ParseException("Illegal number of elements defined. Only one allowed", lineno);
               }
               this.spring = (Beam_Spring_2)super.findElement(super.getNodeNumber(1, param[i + 2].getw().toUpperCase()), globalelementlist);
            }  catch (IllegalArgumentException e) {
               System.out.println(e + "In line " + lineno);
            }
            i += 3;
            this.Element_is_set = true;
         } else if (param[i].getw().toUpperCase().equals("TARGET") && param[i + 1].getw().toUpperCase().equals("=")) {
            if (param[i + 2].is_a_number()) {
               throw new java.text.ParseException("Illegal argument, Target should be defined as [time,timetolerance,targetvalue,targetvaluetolerance]", lineno);
            }
            try {
               this.targettime = super.getNumber(1, param[i + 2].getw().toUpperCase());
               this.timetolerance = super.getNumber(2, param[i + 2].getw().toUpperCase());
               this.target = super.getNumber(3, param[i + 2].getw().toUpperCase());
               this.tolerance = super.getNumber(4, param[i + 2].getw().toUpperCase());
            }  catch (IllegalArgumentException e) {
               throw new java.text.ParseException("Illegal argument or value missing; Target should be defined as [time,timetolerance,targetvalue,targetvaluetolerance]", lineno);
            }
            i += 3;
            this.Target_is_set = true;
         } else if (param[i].getw().toUpperCase().equals("FILENAME") && param[i + 1].getw().toUpperCase().equals("=")) {
            this.filename = new String(param[i + 2].getw().trim());
            i += 3;
            this.File_is_set = true;
         } else if (param[i].getw().toUpperCase().equals("COMPONENT") && param[i + 1].getw().toUpperCase().equals("=")) {
            if (param[i + 2].getw().trim().toUpperCase().equals("FX")) {
               this.component = 0;
            } else if (param[i + 2].getw().trim().toUpperCase().equals("FY")) {
               this.component = 1;
            } else if (param[i + 2].getw().trim().toUpperCase().equals("FZ")) {
               this.component = 2;
            } else if (param[i + 2].getw().trim().toUpperCase().equals("MX")) {
               this.component = 3;
            } else if (param[i + 2].getw().trim().toUpperCase().equals("MY")) {
               this.component = 4;
            } else if (param[i + 2].getw().trim().toUpperCase().equals("MZ")) {
               this.component = 5;
            }
            i += 3;
            this.Component_is_set = true;
         } else {
            throw new java.text.ParseException("Unknown Tracker parameter ", lineno);
         }
      }
   }
   public void parse_Nastran(Token[] param, int lineno, RplVector globalnodelist, RplVector globalelementlist) throws java.text.ParseException {
   }
   public void parse_Gmsh(Token[] param, int lineno, RplVector globalnodelist, RplVector globalelementlist) throws java.text.ParseException {
   }
   public void checkIndata() throws IllegalArgumentException {
      if (!this.Element_is_set) {
         throw new IllegalArgumentException("No element defined for this SpringTracker: " + this.number);
      }
      if (!this.File_is_set) {
         throw new IllegalArgumentException("No Filename defined for this SpringTracker: " + this.number);
      }
   }
   public void print_Gid(int ctrl, double currtime) throws IOException, IllegalArgumentException {
      collectOwnData();
      if (this.Target_is_set) {
         if (super.checkTarget(currtime)) {
            try {
               this.bw = new BufferedWriter(new FileWriter(this.filename + ".target", true));
               this.bw.write("Target was reached at time: " + currtime + " with result: " + this.result + "\n");
               this.bw.flush();
               this.bw.close();
            }  catch (IOException ioe) {
               System.out.println("Error in writing target result file: " + this.filename + ".target");
               throw ioe;
            }
         }
      }
      String out;
      int i;
      switch (ctrl) {
      case run.Tracker.RESULT_HEADER: 
         try {
            this.bw = new BufferedWriter(new FileWriter(this.filename));
            out = new String("# Impact SpringTracker results from tracker number: " + this.number + "\n");
            out += "# The following element is read in the section:\n";
            out += "# \n# ";
            out += this.spring.getNumber() + ": ";
            out += "\n# \n";
            out += "# X: time \t Y: spring force or moment: ";
            out += "\n#\n";
            this.bw.write(out);
            this.bw.flush();
            this.bw.close();
         }  catch (IOException ioe) {
            System.out.println("Error in creating the SpringTracker file: " + this.filename);
            throw ioe;
         }
         return;
      
      case run.Tracker.RESULT: 
         try {
            this.bw = new BufferedWriter(new FileWriter(this.filename, true));
            this.bw.write(currtime + "\t" + this.result + "\n");
            this.bw.flush();
            this.bw.close();
         }  catch (IOException ioe) {
            System.out.println("Error in writing results to the SpringTracker file: " + this.filename);
            throw ioe;
         }
         return;
      
      default: 
         throw new IllegalArgumentException("Unknown parameter for print_Gid in SpringTracker number: " + this.number);
      
      }
   }
   public void setInitialConditions() {
   }
   public int getType() {
      return run.trackers.BeamSpringTracker.TYPE;
   }
}
