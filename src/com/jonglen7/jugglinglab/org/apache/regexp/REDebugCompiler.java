package com.jonglen7.jugglinglab.org.apache.regexp;

/*
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights 
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer. 
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Jakarta-Regexp", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */ 

import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * A subclass of RECompiler which can dump a regular expression program
 * for debugging purposes.
 *
 * @author <a href="mailto:jonl@muppetlabs.com">Jonathan Locke</a>
 * @version $Id: REDebugCompiler.java,v 1.1 2004/02/23 04:31:45 jboyce Exp $
 */
public class REDebugCompiler extends RECompiler
{
    /**
     * Mapping from opcodes to descriptive strings
     */
    static Hashtable<Integer, String> hashOpcode = new Hashtable<Integer, String>();
    static
    {
        hashOpcode.put(Integer.valueOf(RE.OP_RELUCTANTSTAR),    "OP_RELUCTANTSTAR");
        hashOpcode.put(Integer.valueOf(RE.OP_RELUCTANTPLUS),    "OP_RELUCTANTPLUS");
        hashOpcode.put(Integer.valueOf(RE.OP_RELUCTANTMAYBE),   "OP_RELUCTANTMAYBE");
        hashOpcode.put(Integer.valueOf(RE.OP_END),              "OP_END");
        hashOpcode.put(Integer.valueOf(RE.OP_BOL),              "OP_BOL");
        hashOpcode.put(Integer.valueOf(RE.OP_EOL),              "OP_EOL");
        hashOpcode.put(Integer.valueOf(RE.OP_ANY),              "OP_ANY");
        hashOpcode.put(Integer.valueOf(RE.OP_ANYOF),            "OP_ANYOF");
        hashOpcode.put(Integer.valueOf(RE.OP_BRANCH),           "OP_BRANCH");
        hashOpcode.put(Integer.valueOf(RE.OP_ATOM),             "OP_ATOM");
        hashOpcode.put(Integer.valueOf(RE.OP_STAR),             "OP_STAR");
        hashOpcode.put(Integer.valueOf(RE.OP_PLUS),             "OP_PLUS");
        hashOpcode.put(Integer.valueOf(RE.OP_MAYBE),            "OP_MAYBE");
        hashOpcode.put(Integer.valueOf(RE.OP_NOTHING),          "OP_NOTHING");
        hashOpcode.put(Integer.valueOf(RE.OP_GOTO),             "OP_GOTO");
        hashOpcode.put(Integer.valueOf(RE.OP_ESCAPE),           "OP_ESCAPE");
        hashOpcode.put(Integer.valueOf(RE.OP_OPEN),             "OP_OPEN");
        hashOpcode.put(Integer.valueOf(RE.OP_CLOSE),            "OP_CLOSE");
        hashOpcode.put(Integer.valueOf(RE.OP_BACKREF),          "OP_BACKREF");
        hashOpcode.put(Integer.valueOf(RE.OP_POSIXCLASS),       "OP_POSIXCLASS");
        hashOpcode.put(Integer.valueOf(RE.OP_OPEN_CLUSTER),      "OP_OPEN_CLUSTER");
        hashOpcode.put(Integer.valueOf(RE.OP_CLOSE_CLUSTER),      "OP_CLOSE_CLUSTER");
    }

    /**
     * Returns a descriptive string for an opcode.
     * @param opcode Opcode to convert to a string
     * @return Description of opcode
     */
    String opcodeToString(char opcode)
    {
        // Get string for opcode
        String ret =(String)hashOpcode.get(Integer.valueOf(opcode));

        // Just in case we have a corrupt program
        if (ret == null)
        {
            ret = "OP_????";
        }
        return ret;
    }

    /**
     * Return a string describing a (possibly unprintable) character.
     * @param c Character to convert to a printable representation
     * @return String representation of character
     */
    String charToString(char c)
    {
        // If it's unprintable, convert to '\###'
        if (c < ' ' || c > 127)
        {
            return "\\" + (int)c;
        }

        // Return the character as a string
        return String.valueOf(c);
    }

    /**
     * Returns a descriptive string for a node in a regular expression program.
     * @param node Node to describe
     * @return Description of node
     */
    String nodeToString(int node)
    {
        // Get opcode and opdata for node
        char opcode =      instruction[node + RE.offsetOpcode];
        int opdata  = (int)instruction[node + RE.offsetOpdata];

        // Return opcode as a string and opdata value
        return opcodeToString(opcode) + ", opdata = " + opdata;
    }

    /**
     * Inserts a node with a given opcode and opdata at insertAt.  The node relative next
     * pointer is initialized to 0.
     * @param opcode Opcode for new node
     * @param opdata Opdata for new node (only the low 16 bits are currently used)
     * @param insertAt Index at which to insert the new node in the program * /
    void nodeInsert(char opcode, int opdata, int insertAt) {
        System.out.println( "====> " + opcode + " " + opdata + " " + insertAt );
        PrintWriter writer = new PrintWriter( System.out );
        dumpProgram( writer );
        super.nodeInsert( opcode, opdata, insertAt );
        System.out.println( "====< " );
        dumpProgram( writer );
        writer.flush();
    }/**/


    /**
    * Appends a node to the end of a node chain
    * @param node Start of node chain to traverse
    * @param pointTo Node to have the tail of the chain point to * /
    void setNextOfEnd(int node, int pointTo) {
        System.out.println( "====> " + node + " " + pointTo );
        PrintWriter writer = new PrintWriter( System.out );
        dumpProgram( writer );
        super.setNextOfEnd( node, pointTo );
        System.out.println( "====< " );
        dumpProgram( writer );
        writer.flush();
    }/**/


    /**
     * Dumps the current program to a PrintWriter
     * @param p PrintWriter for program dump output
     */
    public void dumpProgram(PrintWriter p)
    {
        // Loop through the whole program
        for (int i = 0; i < lenInstruction; )
        {
            // Get opcode, opdata and next fields of current program node
            char opcode =        instruction[i + RE.offsetOpcode];
            char opdata =        instruction[i + RE.offsetOpdata];
            short next  = (short)instruction[i + RE.offsetNext];

            // Display the current program node
            p.print(i + ". " + nodeToString(i) + ", next = ");

            // If there's no next, say 'none', otherwise give absolute index of next node
            if (next == 0)
            {
                p.print("none");
            }
            else
            {
                p.print(i + next);
            }

            // Move past node
            i += RE.nodeSize;

            // If character class
            if (opcode == RE.OP_ANYOF)
            {
                // Opening bracket for start of char class
                p.print(", [");

                // Show each range in the char class
                int rangeCount = opdata;
                for (int r = 0; r < rangeCount; r++)
                {
                    // Get first and last chars in range
                    char charFirst = instruction[i++];
                    char charLast  = instruction[i++];

                    // Print range as X-Y, unless range encompasses only one char
                    if (charFirst == charLast)
                    {
                        p.print(charToString(charFirst));
                    }
                    else
                    {
                        p.print(charToString(charFirst) + "-" + charToString(charLast));
                    }
                }

                // Annotate the end of the char class
                p.print("]");
            }

            // If atom
            if (opcode == RE.OP_ATOM)
            {
                // Open quote
                p.print(", \"");

                // Print each character in the atom
                for (int len = opdata; len-- != 0; )
                {
                    p.print(charToString(instruction[i++]));
                }

                // Close quote
                p.print("\"");
            }

            // Print a newline
            p.println("");
        }
    }
}
