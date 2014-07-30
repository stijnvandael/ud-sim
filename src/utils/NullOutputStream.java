package utils;

/**
 * 
 * JFreeReport : a free Java reporting library
 * 
 *
 * Project Info:  http://reporting.pentaho.org/
 *
 * (C) Copyright 2001-2007, by Object Refinery Ltd, Pentaho Corporation and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ------------
 * NullOutputStream.java
 * ------------
 * (C) Copyright 2001-2007, by Object Refinery Ltd, Pentaho Corporation and Contributors.
 */

import java.io.IOException;
import java.io.OutputStream;

/**
 * A null output stream. All data written to this stream is ignored.
 *
 * @author Thomas Morgner
 */
public class NullOutputStream extends OutputStream
{
  /**
   * Default constructor.
   */
  public NullOutputStream ()
  {
  }

  /**
   * Writes to the stream (in this case, does nothing).
   *
   * @param i the value.
   * @throws IOException if there is an I/O problem.
   */
  public void write (final int i)
          throws IOException
  {
    // no i wont do anything here ...
  }

  /**
   * Writes to the stream (in this case, does nothing).
   *
   * @param bytes the bytes.
   * @throws IOException if there is an I/O problem.
   */
  public void write (final byte[] bytes)
          throws IOException
  {
    // no i wont do anything here ...
  }

  /**
   * Writes to the stream (in this case, does nothing).
   *
   * @param bytes the bytes.
   * @param off   the start offset in the data.
   * @param len   the number of bytes to write.
   * @throws IOException if there is an I/O problem.
   */
  public void write (final byte[] bytes, final int off, final int len)
          throws IOException
  {
    // no i wont do anything here ...
  }

}