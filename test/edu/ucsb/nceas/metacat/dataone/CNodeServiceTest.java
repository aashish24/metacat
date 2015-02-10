/**
 *  '$RCSfile$'
 *  Copyright: 2010 Regents of the University of California and the
 *              National Center for Ecological Analysis and Synthesis
 *  Purpose: To test the Access Controls in metacat by JUnit
 *
 *   '$Author$'
 *     '$Date$'
 * '$Revision$'
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.ucsb.nceas.metacat.dataone;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.io.IOUtils;
import org.dataone.client.v2.itk.D1Client;
import org.dataone.service.exceptions.BaseException;
import org.dataone.service.exceptions.InvalidSystemMetadata;
import org.dataone.service.exceptions.NotAuthorized;
import org.dataone.service.exceptions.NotFound;
import org.dataone.service.exceptions.NotImplemented;
import org.dataone.service.exceptions.ServiceFailure;
import org.dataone.service.types.v1.AccessPolicy;
import org.dataone.service.types.v1.AccessRule;
import org.dataone.service.types.v1.Checksum;
import org.dataone.service.types.v1.DescribeResponse;
import org.dataone.service.types.v1.Event;
import org.dataone.service.types.v1.Identifier;
import org.dataone.service.types.v1.NodeReference;
import org.dataone.service.types.v1.NodeType;
import org.dataone.service.types.v1.ObjectFormatIdentifier;
import org.dataone.service.types.v1.ObjectList;
import org.dataone.service.types.v1.Permission;
import org.dataone.service.types.v1.Replica;
import org.dataone.service.types.v1.ReplicationPolicy;
import org.dataone.service.types.v1.ReplicationStatus;
import org.dataone.service.types.v1.Session;
import org.dataone.service.types.v1.Subject;
import org.dataone.service.types.v2.Log;
import org.dataone.service.types.v2.Node;
import org.dataone.service.types.v2.NodeList;
import org.dataone.service.types.v2.ObjectFormat;
import org.dataone.service.types.v2.ObjectFormatList;
import org.dataone.service.types.v2.SystemMetadata;
import org.dataone.service.util.Constants;

import edu.ucsb.nceas.metacat.dataone.CNodeService;

/**
 * A JUnit test for testing the dataone CNCore implementation
 */
public class CNodeServiceTest extends D1NodeServiceTest {   
    
    /**
    * constructor for the test
    */
    public CNodeServiceTest(String name)
    {
        super(name);
    }

	/**
	 * Create a suite of tests to be run together
	 */
	public static Test suite() 
	{
		TestSuite suite = new TestSuite();
		suite.addTest(new CNodeServiceTest("initialize"));
		
		suite.addTest(new CNodeServiceTest("testChecksum"));
		suite.addTest(new CNodeServiceTest("testCreate"));
		suite.addTest(new CNodeServiceTest("testGet"));
		suite.addTest(new CNodeServiceTest("testGetFormat"));
		suite.addTest(new CNodeServiceTest("testGetLogRecords"));
		suite.addTest(new CNodeServiceTest("testGetSystemMetadata"));
		suite.addTest(new CNodeServiceTest("testIsAuthorized"));
		suite.addTest(new CNodeServiceTest("testListFormats"));
		suite.addTest(new CNodeServiceTest("testListNodes"));
		suite.addTest(new CNodeServiceTest("testObjectFormatNotFoundException"));
		suite.addTest(new CNodeServiceTest("testRegisterSystemMetadata"));
		suite.addTest(new CNodeServiceTest("testReplicationPolicy"));
		suite.addTest(new CNodeServiceTest("testReplicationStatus"));
		suite.addTest(new CNodeServiceTest("testReserveIdentifier"));
		suite.addTest(new CNodeServiceTest("testSearch"));
		suite.addTest(new CNodeServiceTest("testSetAccessPolicy"));
		suite.addTest(new CNodeServiceTest("testSetOwner"));
		suite.addTest(new CNodeServiceTest("readDeletedObject"));
		suite.addTest(new CNodeServiceTest("testGetSID"));
	
		return suite;
	}
	
	
	/**
	 * test for registering standalone system metadata
	 */
	public Identifier testRegisterSystemMetadata() {
	    printTestHeader("testRegisterSystemMetadata");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testRegisterSystemMetadata." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
			Identifier retGuid = CNodeService.getInstance(request).registerSystemMetadata(session, guid, sysmeta);
			assertEquals(guid.getValue(), retGuid.getValue());
			return retGuid;
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
        return null;
	}
	
	/**
	 * test for getting system metadata
	 */
	public void testGetSystemMetadata() {
	    printTestHeader("testGetSystemMetadata");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testGetSystemMetadata." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
			Identifier retGuid = CNodeService.getInstance(request).registerSystemMetadata(session, guid, sysmeta);
			assertEquals(guid.getValue(), retGuid.getValue());
			// get it
			SystemMetadata retSysmeta = CNodeService.getInstance(request).getSystemMetadata(session, guid);
			// check it
			assertEquals(sysmeta.getIdentifier().getValue(), retSysmeta.getIdentifier().getValue());
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testGetLogRecords() {
	    printTestHeader("testGetLogRecords");
	    try {

		    Session session = getTestSession();
		    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		    Date fromDate = sdf.parse("2010-01-01");
		    Date toDate = new Date();
		    Event event = Event.CREATE;
		    int start = 0;
		    int count = 1;
	    
		    Log log = CNodeService.getInstance(request).getLogRecords(session, fromDate, toDate, 
		    	event.xmlValue(), null, start, count);
		    assertNotNull(log);
		    assertTrue(log.getCount() == count);
		    assertTrue(log.getStart() == start);
		    assertTrue(log.getTotal() > 0);
	    } catch (Exception e) {
		    e.printStackTrace();
		    fail("Unexpected error: " + e.getMessage());
	    } 
	}
	
	public void testCreate() {
	    printTestHeader("testCreate");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testCreate." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
			Identifier pid = CNodeService.getInstance(request).create(session, guid, object, sysmeta);
			assertEquals(guid, pid);
        } catch(Exception e) {
        	e.printStackTrace();
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testGet() {
	    printTestHeader("testGet");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testGet." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
			Identifier pid = CNodeService.getInstance(request).create(session, guid, object, sysmeta);
			assertEquals(guid.getValue(), pid.getValue());
			System.out.println("the pid is+++++++++++++++++++++++++"+guid.getValue());
			// get it
			InputStream retObject = CNodeService.getInstance(request).get(session, pid);
			// check it
			object.reset();
			assertTrue(IOUtils.contentEquals(object, retObject));
        } catch(Exception e) {
        	e.printStackTrace();
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testChecksum() {
	    printTestHeader("testChecksum");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testChecksum." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
			// save it
			Identifier retGuid = CNodeService.getInstance(request).registerSystemMetadata(session, guid, sysmeta);
			assertEquals(guid.getValue(), retGuid.getValue());
			// check it
			Checksum checksum = CNodeService.getInstance(request).getChecksum(session, guid);
			assertEquals(sysmeta.getChecksum().getValue(), checksum.getValue());
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testListNodes() {
	    printTestHeader("testListNodes");

	    try {
	    	CNodeService.getInstance(request).listNodes();
        } catch(NotImplemented e) {
        	// expecting not implemented
            assertTrue(true);
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testReserveIdentifier() {
	    printTestHeader("testReserveIdentifier");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testReserveIdentifier." + System.currentTimeMillis());
			// reserve it
			Identifier resultPid = CNodeService.getInstance(request).reserveIdentifier(session, guid);
			assertNotNull(resultPid);
			assertEquals(guid.getValue(), resultPid.getValue());
	    } catch(NotImplemented ni) {
        	// this is not implemented in Metacat
            assertTrue(true);	
        } catch(Exception e) {
        	e.printStackTrace();
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testSearch() {
	    printTestHeader("testSearch");

	    try {
            Session session = getTestSession();
			
			// search for objects, but expect a NotImplemented exception
			try {
				ObjectList objectList = CNodeService.getInstance(request).search(session, null, null);
			} catch (NotImplemented ne) {
				assertTrue(true);
				return;
			}
			fail("Metacat should not implement CN.search");
			
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testSetOwner() {
	    printTestHeader("testSetOwner");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testSetOwner." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
			long serialVersion = 1L;
			// save it
			Identifier retGuid = CNodeService.getInstance(request).registerSystemMetadata(session, guid, sysmeta);
			assertEquals(guid.getValue(), retGuid.getValue());
			Subject rightsHolder = new Subject();
			rightsHolder.setValue("newUser");
			// set it
			Identifier retPid = CNodeService.getInstance(request).setRightsHolder(session, guid, rightsHolder, serialVersion);
			assertEquals(guid, retPid);
			// get it
			sysmeta = CNodeService.getInstance(request).getSystemMetadata(session, guid);
			assertNotNull(sysmeta);
			// check it
			assertTrue(rightsHolder.equals(sysmeta.getRightsHolder()));
			
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testSetAccessPolicy() {
	    printTestHeader("testSetAccessPolicy");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testSetAccessPolicy." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
	    long serialVersion = 1L;

			// save it
			Identifier retGuid = CNodeService.getInstance(request).registerSystemMetadata(session, guid, sysmeta);
			assertEquals(guid.getValue(), retGuid.getValue());
			AccessPolicy accessPolicy = new AccessPolicy();
			AccessRule accessRule = new AccessRule();
			accessRule.addPermission(Permission.WRITE);
			Subject publicSubject = new Subject();
			publicSubject.setValue(Constants.SUBJECT_PUBLIC);
			accessRule.addSubject(publicSubject);
			accessPolicy.addAllow(accessRule);
			// set it
			boolean result = CNodeService.getInstance(request).setAccessPolicy(session, guid, accessPolicy, serialVersion );
			assertTrue(result);
			// check it
			result = CNodeService.getInstance(request).isAuthorized(session, guid, Permission.WRITE);
			assertTrue(result);
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testIsAuthorized() {
	    printTestHeader("testIsAuthorized");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testIsAuthorized." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
			// save it
			Identifier retGuid = CNodeService.getInstance(request).registerSystemMetadata(session, guid, sysmeta);
			assertEquals(guid.getValue(), retGuid.getValue());
			// check it
			Subject publicSubject = new Subject();
			publicSubject.setValue(Constants.SUBJECT_PUBLIC);
			session.setSubject(publicSubject);
			// public read
			boolean result = CNodeService.getInstance(request).isAuthorized(session, guid, Permission.READ);
			assertTrue(result);
			// not public write
			try {
				result = false;
				result = CNodeService.getInstance(request).isAuthorized(session, guid, Permission.WRITE);
				fail("Public WRITE should be denied");
			} catch (NotAuthorized nae) {
				result = true;
			}
			assertTrue(result);
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testReplicationPolicy() {
	    printTestHeader("testReplicationPolicy");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testReplicationPolicy." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
	    long serialVersion = 1L;

			// save it
			Identifier retGuid = CNodeService.getInstance(request).registerSystemMetadata(session, guid, sysmeta);
			assertEquals(guid.getValue(), retGuid.getValue());
			
			ReplicationPolicy policy = new ReplicationPolicy();
			NodeReference node = new NodeReference();
			node.setValue("testNode");
			policy.addPreferredMemberNode(node );
			// set it
			boolean result = CNodeService.getInstance(request).setReplicationPolicy(session, guid, policy, serialVersion);
			assertTrue(result);
			// get it
			sysmeta = CNodeService.getInstance(request).getSystemMetadata(session, guid);
			assertNotNull(sysmeta);
			// check it
			assertEquals(policy.getPreferredMemberNode(0).getValue(), sysmeta.getReplicationPolicy().getPreferredMemberNode(0).getValue());
			
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	public void testReplicationStatus() {
	    printTestHeader("testReplicationStatus");

	    try {
            Session session = getTestSession();
			Identifier guid = new Identifier();
			guid.setValue("testReplicationStatus." + System.currentTimeMillis());
			InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
			SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
			Replica replica = new Replica();
			NodeReference replicaMemberNode = new NodeReference();
			replicaMemberNode.setValue("testNode");
			replica.setReplicationStatus(ReplicationStatus.REQUESTED);
			replica.setReplicaMemberNode(replicaMemberNode);
			replica.setReplicaVerified(Calendar.getInstance().getTime());
			sysmeta.addReplica(replica );
			// save it
			Identifier retGuid = CNodeService.getInstance(request).registerSystemMetadata(session, guid, sysmeta);
			assertEquals(guid.getValue(), retGuid.getValue());
			// set it
			ReplicationStatus status = ReplicationStatus.COMPLETED;
			BaseException failure = new NotAuthorized("000", "Mock exception for " + this.getClass().getName());
			boolean result = CNodeService.getInstance(request).setReplicationStatus(session, guid, replicaMemberNode, status, failure);
			assertTrue(result);
			// get it
			sysmeta = CNodeService.getInstance(request).getSystemMetadata(session, guid);
			assertNotNull(sysmeta);
			// check it
			assertEquals(status, sysmeta.getReplica(0).getReplicationStatus());
			
        } catch(Exception e) {
            fail("Unexpected error: " + e.getMessage());
        }
	}
	
	/**
	 * Run an initial test that always passes to check that the test harness is
	 * working.
	 */
	public void initialize() 
	{
	    printTestHeader("initialize");
		assertTrue(1 == 1);
	}
	
	/**
	 * We want to act as the CN itself
	 * @throws ServiceFailure 
	 * @throws Exception 
	 */
	@Override
	public Session getTestSession() throws Exception {
		Session session = super.getTestSession();
		
		// use the first CN we find in the nodelist
        NodeList nodeList = D1Client.getCN().listNodes();
        for (Node node : nodeList.getNodeList()) {
            if ( node.getType().equals(NodeType.CN) ) {
                
                List<Subject> subjects = node.getSubjectList();
                for (Subject subject : subjects) {
                   session.setSubject(subject);
                   // we are done here
                   return session;
                }
            }
        }
        // in case we didn't find it
        return session;
	}
	

	/**
	 * test to list the object formats registered in metacat
	 */
	public void testListFormats() {
		
    printTestHeader("testListFormats");
    
    // make sure we are set up
    setUpFormats();
    
    // there should be at least 59 formats in the list
  	int formatsCount = 59;
  	ObjectFormatList objectFormatList;
  	
  	try {
	    objectFormatList = CNodeService.getInstance(request).listFormats();
	  	assertTrue(objectFormatList.getTotal() >= formatsCount);
  	
  	} catch (ServiceFailure e) {
  		fail("Could not get the object format list: " + e.getMessage());

    } catch (NotImplemented e) {
  		fail("Could not get the object format list: " + e.getMessage());

    }
    
	}
	
  /**
   * Test getting a single object format from the registered list
   */
  public void testGetFormat() {
  	
    printTestHeader("testGetFormat");

    // make sure we are set up
    setUpFormats();
    
    String knownFormat = "text/plain";
    ObjectFormatIdentifier fmtid = new ObjectFormatIdentifier();
    fmtid.setValue(knownFormat);
  	
    try {
	    
			String result = 
				CNodeService.getInstance(request).getFormat(fmtid).getFormatId().getValue();
	  	System.out.println("Expected result: " + knownFormat);
	  	System.out.println("Found    result: " + result);
	  	assertTrue(result.equals(knownFormat));
  
    } catch (NullPointerException npe) {	  
	    fail("The returned format was null: " + npe.getMessage());
    
    } catch (NotFound nfe) {     
    	fail("The format " + knownFormat + " was not found: " + nfe.getMessage());
    	
    } catch (ServiceFailure sfe) {
    	fail("The format " + knownFormat + " was not found: " + sfe.getMessage());

    } catch (NotImplemented nie) {
    	fail("The getFormat() method has not been implemented: " + nie.getMessage());

    }
  	
  }
	
  /**
   * Test getting a non-existent object format, returning NotFound
   */
  public void testObjectFormatNotFoundException() {
  
    printTestHeader("testObjectFormatNotFoundException");

    ObjectFormatIdentifier fmtid = new ObjectFormatIdentifier();
  	String badFormat = "text/bad-format";
  	fmtid.setValue(badFormat);
  	
  	try {
  		
	    ObjectFormat objectFormat = 
	    	CNodeService.getInstance(request).getFormat(fmtid);
      
  	} catch (Exception e) {
	    
  		assertTrue(e instanceof NotFound);
  	}
  	
  }
  
  public void readDeletedObject() {
      printTestHeader("testCreate");

      try {
          Session session = getTestSession();
          Identifier guid = new Identifier();
          guid.setValue("testCreate." + System.currentTimeMillis());
          InputStream object = new ByteArrayInputStream("test".getBytes("UTF-8"));
          SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object);
          Identifier pid = CNodeService.getInstance(request).create(session, guid, object, sysmeta);
          assertEquals(guid, pid);
          
          Thread.sleep(3000);
          // use MN admin to delete
          session = getMNSession();
          Identifier deletedPid = CNodeService.getInstance(request).delete(session, pid);
          System.out.println("after deleting");
          assertEquals(pid.getValue(), deletedPid.getValue());
          // check that we cannot get the object
          session = getTestSession();
          InputStream deletedObject = null;
          try {
              //System.out.println("before read ===============");
              deletedObject = CNodeService.getInstance(request).get(session, deletedPid);
              //System.out.println("after read ===============");
          } catch (NotFound nf) {
              //System.out.println("the exception is1 "+nf.getMessage());
              //nf.printStackTrace();
              assertTrue(nf.getMessage().contains("deleted"));
          }
          try {
              //System.out.println("before read ===============");
              SystemMetadata sysmeta2 = CNodeService.getInstance(request).getSystemMetadata(session, deletedPid);
              //System.out.println("after read ===============");
          } catch (NotFound nf) {
              //System.out.println("the exception is "+nf.getMessage());
              //nf.printStackTrace();
              assertTrue(nf.getMessage().contains("deleted"));
          }
          
          try {
              //System.out.println("before read ===============");
              DescribeResponse describeResponse = CNodeService.getInstance(request).describe(session, pid);
              //System.out.println("after read ===============");
          } catch (NotFound nf) {
              //System.out.println("the exception is "+nf.getMessage());
              //nf.printStackTrace();
              assertTrue(nf.getMessage().contains("deleted"));
          }
          
          try {
              //System.out.println("before read ===============");
              Checksum checksum = CNodeService.getInstance(request).getChecksum(session, pid);
              //System.out.println("after read ===============");
          } catch (NotFound nf) {
              //System.out.println("the exception 3 is "+nf.getMessage());
              //nf.printStackTrace();
              assertTrue(nf.getMessage().contains("deleted"));
          }
          
          try {
              //System.out.println("before read ===============");
              boolean isAuthorized = 
                      CNodeService.getInstance(request).isAuthorized(session, pid, Permission.READ);
              //System.out.println("after read ===============");
          } catch (NotFound nf) {
              //System.out.println("the exception 4 is "+nf.getMessage());
              //nf.printStackTrace();
              assertTrue(nf.getMessage().contains("deleted"));
          }
          
         
          
          assertNull(deletedObject);
      } catch(Exception e) {
          e.printStackTrace();
          fail("Unexpected error: " + e.getMessage());
      }
  }
  
  /**
   * Test the method - get api  for a speicified SID
   */
  public void testGetSID() {
      String str1 = "object1";
      String str2 = "object2";
      String str3 = "object3";
      try {
          //insert test documents with a series id
          Session session = getTestSession();
          Identifier guid = new Identifier();
          guid.setValue(generateDocumentId());
          InputStream object1 = new ByteArrayInputStream(str1.getBytes("UTF-8"));
          SystemMetadata sysmeta = createSystemMetadata(guid, session.getSubject(), object1);
          String sid1= "sid."+System.nanoTime();
          Identifier seriesId = new Identifier();
          seriesId.setValue(sid1);
          System.out.println("the first sid is "+seriesId.getValue());
          sysmeta.setSeriesId(seriesId);
          CNodeService.getInstance(request).create(session, guid, object1, sysmeta);
          System.out.println("the first pid is "+guid.getValue());
          //test the get(pid) for v2
          InputStream result = CNodeService.getInstance(request).get(session, guid);
          // go back to beginning of original stream
          object1.reset();
          // check
          assertTrue(object1.available() > 0);
          assertTrue(result.available() > 0);
          assertTrue(IOUtils.contentEquals(result, object1));
          // test the get(id) for v2
          InputStream result1 = CNodeService.getInstance(request).get(session, seriesId);
          object1.reset();
          // check
          assertTrue(object1.available() > 0);
          assertTrue(result1.available() > 0);
          assertTrue(IOUtils.contentEquals(result1, object1));
          //test the get(pid) for v1
          InputStream result2 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, guid);
          object1.reset();
          // check
          assertTrue(object1.available() > 0);
          assertTrue(result2.available() > 0);
          assertTrue(IOUtils.contentEquals(result2, object1));
          //test the get(sid) for v1
          try {
              InputStream result3 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, seriesId);
              fail("the get(sid) methoud should throw a not found exception for the sid "+seriesId.getValue());
          } catch (NotFound ee) {
              
          }
          SystemMetadata metadata = CNodeService.getInstance(request).getSystemMetadata(session, seriesId);
          assertTrue(metadata.getIdentifier().getValue().equals(guid.getValue()));
          assertTrue(metadata.getSeriesId().getValue().equals(seriesId.getValue()));
          DescribeResponse describeResponse = CNodeService.getInstance(request).describe(session, seriesId);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata.getFormatId().getValue());
          
          metadata = CNodeService.getInstance(request).getSystemMetadata(session, guid);
          assertTrue(metadata.getIdentifier().getValue().equals(guid.getValue()));
          assertTrue(metadata.getSeriesId().getValue().equals(seriesId.getValue()));
          describeResponse = CNodeService.getInstance(request).describe(session, guid);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata.getFormatId().getValue());
          
          org.dataone.service.types.v1.SystemMetadata sys1=edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).getSystemMetadata(session, guid);
          assertTrue(metadata.getIdentifier().getValue().equals(guid.getValue()));
          
          try {
              org.dataone.service.types.v1.SystemMetadata sys2=edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).getSystemMetadata(session, seriesId);
              fail("the getSystemMetadata(sid) methoud should throw a not found exception for the sid "+seriesId.getValue());
          } catch(NotFound nf2) {
              
          }
          
          describeResponse = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).describe(session, guid);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), sys1.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), sys1.getFormatId().getValue());
          try {
              describeResponse = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).describe(session, seriesId);
              fail("the describe(sid) methoud should throw a not found exception for the sid "+seriesId.getValue());
          } catch(NotFound nf2) {
              
          }
          
          Checksum sum = CNodeService.getInstance(request).getChecksum(session, guid);
          assertTrue(sum.getValue().equals("5b78f9689b9aab1ebc0f3c1df916dd97"));
          
          try {
              sum = CNodeService.getInstance(request).getChecksum(session, seriesId);
              fail("the getCheckSum shouldn't work for sid");
          } catch(NotFound nf3) {
              
          }
          
          sum = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).getChecksum(session, guid);
          assertTrue(sum.getValue().equals("5b78f9689b9aab1ebc0f3c1df916dd97"));
          
          try {
              sum = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).getChecksum(session, seriesId);
              fail("the getCheckSum shouldn't work for sid");
          } catch(NotFound nf3) {
              
          }
          
          boolean isAuthorized = 
                  CNodeService.getInstance(request).isAuthorized(session, guid, Permission.READ);
          assertEquals(isAuthorized, true);
          
          isAuthorized = 
                  CNodeService.getInstance(request).isAuthorized(session, seriesId, Permission.READ);
          assertEquals(isAuthorized, true);
          
          isAuthorized = 
                  edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).isAuthorized(session, guid, Permission.READ);
          assertEquals(isAuthorized, true);
          
          try {
              isAuthorized = 
                      edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).isAuthorized(session, seriesId, Permission.READ);
              fail("we can't reach here since the v1 isAuthorized method doesn't suppport series id");
          } catch (NotFound e) {
              
          }

          //do a update with the same series id
          Thread.sleep(1000);
          Identifier newPid = new Identifier();
          newPid.setValue(generateDocumentId()+"1");
          System.out.println("the second pid is "+newPid.getValue());
          InputStream object2 = new ByteArrayInputStream(str2.getBytes("UTF-8"));
          SystemMetadata newSysMeta = createSystemMetadata(newPid, session.getSubject(), object2);
          newSysMeta.setObsoletes(guid);
          newSysMeta.setSeriesId(seriesId);
          //CNodeService.getInstance(request).update(session, guid, object2, newPid, newSysMeta);
          CNodeService.getInstance(request).create(session, newPid, object2, newSysMeta);
          //update the system metadata of previous version.
          sysmeta.setObsoletedBy(newPid);
          CNodeService.getInstance(request).updateSystemMetadata(session, guid, sysmeta);
          InputStream result4 = CNodeService.getInstance(request).get(session, guid);
          // go back to beginning of original stream
          object1.reset();
          // check
          assertTrue(object1.available() > 0);
          assertTrue(result4.available() > 0);
          assertTrue(IOUtils.contentEquals(result4, object1));
          
          InputStream result5 = CNodeService.getInstance(request).get(session, newPid);
          // go back to beginning of original stream
          object2.reset();
          // check
          assertTrue(object2.available() > 0);
          assertTrue(result5.available() > 0);
          assertTrue(IOUtils.contentEquals(result5, object2));
          

          InputStream result6 = CNodeService.getInstance(request).get(session, seriesId);
          object2.reset();
          // check
          assertTrue(object2.available() > 0);
          assertTrue(result6.available() > 0);
          assertTrue(IOUtils.contentEquals(result6, object2));
          //test the get(pid) for v1
          InputStream result7 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, guid);
          //System.out.println("+++++++++++++++++++++"+IOUtils.toString(result7));
          object1.reset();
          // check
          assertTrue(object1.available() > 0);
          assertTrue(result7.available() > 0);
          assertTrue(IOUtils.contentEquals(result7, object1));
          
          InputStream result8 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, newPid);
          object2.reset();
          // check
          assertTrue(object2.available() > 0);
          assertTrue(result8.available() > 0);
          assertTrue(IOUtils.contentEquals(result8, object2));
          //test the get(sid) for v1
          try {
              InputStream result3 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, seriesId);
              fail("the get(sid) methoud should throw a not found exception for the sid "+seriesId.getValue());
          } catch (NotFound ee) {
              
          }
          
          SystemMetadata metadata1 = CNodeService.getInstance(request).getSystemMetadata(session, seriesId);
          assertTrue(metadata1.getIdentifier().getValue().equals(newPid.getValue()));
          assertTrue(metadata1.getSeriesId().getValue().equals(seriesId.getValue()));
          describeResponse = CNodeService.getInstance(request).describe(session, seriesId);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata1.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata1.getFormatId().getValue());
          
          SystemMetadata metadata2 = CNodeService.getInstance(request).getSystemMetadata(session, guid);
          assertTrue(metadata2.getIdentifier().getValue().equals(guid.getValue()));
          assertTrue(metadata2.getSeriesId().getValue().equals(seriesId.getValue()));
          describeResponse = CNodeService.getInstance(request).describe(session, guid);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata2.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata2.getFormatId().getValue());
          
          SystemMetadata metadata3 = CNodeService.getInstance(request).getSystemMetadata(session, newPid);
          assertTrue(metadata3.getIdentifier().getValue().equals(newPid.getValue()));
          assertTrue(metadata3.getSeriesId().getValue().equals(seriesId.getValue()));
          describeResponse = CNodeService.getInstance(request).describe(session, newPid);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata3.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata3.getFormatId().getValue());
          
          //do another update with different series id
          Thread.sleep(1000);
          String sid2 = "sid."+System.nanoTime();
          Identifier seriesId2= new Identifier();
          seriesId2.setValue(sid2);
          System.out.println("the second sid is "+seriesId2.getValue());
          Identifier newPid2 = new Identifier();
          newPid2.setValue(generateDocumentId()+"2");
          System.out.println("the third pid is "+newPid2.getValue());
          InputStream object3 = new ByteArrayInputStream(str3.getBytes("UTF-8"));
          SystemMetadata sysmeta3 = createSystemMetadata(newPid2, session.getSubject(), object3);
          sysmeta3.setObsoletes(newPid);
          sysmeta3.setSeriesId(seriesId2);
          //CNodeService.getInstance(request).update(session, newPid, object3, newPid2, sysmeta3);
          CNodeService.getInstance(request).create(session, newPid2, object3, sysmeta3);
          //update the system metadata of the previous version 
          newSysMeta.setObsoletedBy(newPid2);
          CNodeService.getInstance(request).updateSystemMetadata(session, newPid, newSysMeta);
          
          InputStream result9 = CNodeService.getInstance(request).get(session, guid);
          // go back to beginning of original stream
          object1.reset();
          // check
          assertTrue(object1.available() > 0);
          assertTrue(result9.available() > 0);
          assertTrue(IOUtils.contentEquals(result9, object1));
          
          InputStream result10 = CNodeService.getInstance(request).get(session, newPid);
          // go back to beginning of original stream
          object2.reset();
          // check
          assertTrue(object2.available() > 0);
          assertTrue(result10.available() > 0);
          assertTrue(IOUtils.contentEquals(result10, object2));
          
          
          InputStream result11 = CNodeService.getInstance(request).get(session, newPid2);
          // go back to beginning of original stream
          object3.reset();
          // check
          assertTrue(object3.available() > 0);
          assertTrue(result11.available() > 0);
          assertTrue(IOUtils.contentEquals(result11, object3));
          
          InputStream result12 = CNodeService.getInstance(request).get(session, seriesId2);
          object3.reset();
          // check
          assertTrue(object3.available() > 0);
          assertTrue(result12.available() > 0);
          assertTrue(IOUtils.contentEquals(result12, object3));
          
          InputStream result16 = CNodeService.getInstance(request).get(session, seriesId);
          object2.reset();
          // check
          assertTrue(object2.available() > 0);
          assertTrue(result16.available() > 0);
          assertTrue(IOUtils.contentEquals(result16, object2));
         
          //test the get(pid) for v1
          InputStream result13 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, guid);
          object1.reset();
          // check
          assertTrue(object1.available() > 0);
          assertTrue(result13.available() > 0);
          assertTrue(IOUtils.contentEquals(result13, object1));
          
          InputStream result14 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, newPid);
          object2.reset();
          // check
          assertTrue(object2.available() > 0);
          assertTrue(result14.available() > 0);
          assertTrue(IOUtils.contentEquals(result14, object2));
          
          InputStream result15 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, newPid2);
          object3.reset();
          // check
          assertTrue(object3.available() > 0);
          assertTrue(result15.available() > 0);
          assertTrue(IOUtils.contentEquals(result15, object3));
          
          SystemMetadata metadata4 = CNodeService.getInstance(request).getSystemMetadata(session, seriesId);
          assertTrue(metadata4.getIdentifier().getValue().equals(newPid.getValue()));
          assertTrue(metadata4.getSeriesId().getValue().equals(seriesId.getValue()));
          describeResponse = CNodeService.getInstance(request).describe(session, seriesId);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata4.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata4.getFormatId().getValue());
          
          SystemMetadata metadata5 = CNodeService.getInstance(request).getSystemMetadata(session, seriesId2);
          assertTrue(metadata5.getIdentifier().getValue().equals(newPid2.getValue()));
          assertTrue(metadata5.getSeriesId().getValue().equals(seriesId2.getValue()));
          describeResponse = CNodeService.getInstance(request).describe(session, seriesId2);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata5.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata5.getFormatId().getValue());
          
          SystemMetadata metadata6 = CNodeService.getInstance(request).getSystemMetadata(session, guid);
          assertTrue(metadata6.getIdentifier().getValue().equals(guid.getValue()));
          assertTrue(metadata6.getSeriesId().getValue().equals(seriesId.getValue()));
          describeResponse = CNodeService.getInstance(request).describe(session, guid);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata6.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata6.getFormatId().getValue());
          
          SystemMetadata metadata7 = CNodeService.getInstance(request).getSystemMetadata(session, newPid);
          assertTrue(metadata7.getIdentifier().getValue().equals(newPid.getValue()));
          assertTrue(metadata7.getSeriesId().getValue().equals(seriesId.getValue()));
          describeResponse = CNodeService.getInstance(request).describe(session, newPid);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata7.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata7.getFormatId().getValue());
          
          SystemMetadata metadata8 = CNodeService.getInstance(request).getSystemMetadata(session, newPid2);
          assertTrue(metadata8.getIdentifier().getValue().equals(newPid2.getValue()));
          assertTrue(metadata8.getSeriesId().getValue().equals(seriesId2.getValue()));
          describeResponse = CNodeService.getInstance(request).describe(session, newPid2);
          assertEquals(describeResponse.getDataONE_Checksum().getValue(), metadata8.getChecksum().getValue());
          assertEquals(describeResponse.getDataONE_ObjectFormatIdentifier().getValue(), metadata8.getFormatId().getValue());
          
          
          
          System.out.println("here===========================");
          //test the get(sid) for v1
          try {
              InputStream result3 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, seriesId);
              fail("the get(sid) methoud should throw a not found exception for the sid "+seriesId.getValue());
          } catch (NotFound ee) {
              
          }
          
          //test the get(sid) for v1
          try {
              InputStream result3 = edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, seriesId2);
              fail("the get(sid) methoud should throw a not found exception for the sid "+seriesId.getValue());
          } catch (NotFound ee) {
              
          }
          
          //test to get non-existing id for v2
          try {
           // the pid should be null when we try to get a no-exist sid
              Identifier non_exist_sid = new Identifier();
              non_exist_sid.setValue("no-sid-exist-123qwe");
              InputStream result3 = CNodeService.getInstance(request).get(session, non_exist_sid);
              fail("the get(sid) methoud should throw a not found exception for the sid "+seriesId.getValue());
          } catch (NotFound ee) {
              
          }
          
          try {
              // the pid should be null when we try to get a no-exist sid
                 Identifier non_exist_sid = new Identifier();
                 non_exist_sid.setValue("no-sid-exist-123qwe");
                 SystemMetadata result3 = CNodeService.getInstance(request).getSystemMetadata(session, non_exist_sid);
                 fail("the getSystemMetadata(sid) methoud should throw a not found exception for the sid "+seriesId.getValue());
          } catch (NotFound ee) {
                 
          }
          
          try {
              // the pid should be null when we try to get a no-exist sid
                 Identifier non_exist_sid = new Identifier();
                 non_exist_sid.setValue("no-sid-exist-123qwe");
                  CNodeService.getInstance(request).describe(session, non_exist_sid);
                 fail("the describe(sid) methoud should throw a not found exception for the sid "+seriesId.getValue());
             } catch (NotFound ee) {
                 
             }
          
          
          //do another update with invalid series ids
          Thread.sleep(1000);
          Identifier newPid3 = new Identifier();
          newPid3.setValue(generateDocumentId()+"3");
          System.out.println("the third pid is "+newPid3.getValue());
          InputStream object4 = new ByteArrayInputStream(str3.getBytes("UTF-8"));
          SystemMetadata sysmeta4 = createSystemMetadata(newPid3, session.getSubject(), object4);
          sysmeta4.setObsoletes(newPid2);
          sysmeta4.setSeriesId(seriesId);
          try {
              CNodeService.getInstance(request).create(session, newPid3, object4, sysmeta4);
              fail("we can't reach here since the sid is using an old one ");
          } catch (InvalidSystemMetadata eee) {
              
          } 
          
          sysmeta4.setSeriesId(newPid3);
          try {
              CNodeService.getInstance(request).create(session, newPid3, object4, sysmeta4);
              fail("we can't reach here since the sid is using the pid ");
          } catch (InvalidSystemMetadata eee) {
              
          } 
          
          //test archive a series id by v1
          try {
              edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).archive(session, seriesId2);
              fail("we can't reach here since the v1 archive method doesn't support the sid ");
          } catch (NotFound nf2) {
              
          }
          
          // test delete a series id by v1
          Session mnSession = getMNSession();
          try {
              edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).delete(mnSession, seriesId2);
              fail("we can't reach here since the v1 delete method doesn't support the sid ");
          } catch (NotFound nf2) {
              
          }
          
          // test archive a series id by v2
          CNodeService.getInstance(request).archive(session, seriesId2);
          SystemMetadata archived = CNodeService.getInstance(request).getSystemMetadata(session, seriesId2);
          assertTrue(archived.getArchived());
          archived = CNodeService.getInstance(request).getSystemMetadata(session, newPid2);
          assertTrue(archived.getArchived());
          
          // test delete a series id by v2
          CNodeService.getInstance(request).delete(mnSession, seriesId2);
          try {
              CNodeService.getInstance(request).get(session, seriesId2);
              fail("we can't reach here since the series id was deleted ");
          } catch (NotFound nf3) {
              System.out.println("the message is ============="+nf3.getMessage());
              //assertTrue(nf3.getMessage().indexOf("delete") >0);
          }
          
          try {
              CNodeService.getInstance(request).get(session, newPid2);
              fail("we can't reach here since the series id was deleted ");
          } catch (NotFound nf3) {
              //System.out.println("the message is ============="+nf3.getMessage());
              assertTrue(nf3.getMessage().indexOf("delete") >0);
          }
          
          try {
              edu.ucsb.nceas.metacat.dataone.v1.CNodeService.getInstance(request).get(session, newPid2);
              fail("we can't reach here since the series id was deleted ");
          } catch (NotFound nf3) {
              System.out.println("the message is ============="+nf3.getMessage());
              assertTrue(nf3.getMessage().indexOf("delete") >0);
          }
          
          //archive seriesId
          CNodeService.getInstance(request).archive(mnSession, seriesId);
          archived = CNodeService.getInstance(request).getSystemMetadata(session, seriesId);
          assertTrue(archived.getArchived());
          archived = CNodeService.getInstance(request).getSystemMetadata(session, newPid);
          assertTrue(archived.getArchived());
          
          
          //delete seriesId
          CNodeService.getInstance(request).delete(mnSession, seriesId);
          try {
              CNodeService.getInstance(request).get(session, newPid);
              fail("we can't reach here since the series id was deleted ");
          } catch (NotFound nf3) {
              //System.out.println("the message is ============="+nf3.getMessage());
              assertTrue(nf3.getMessage().indexOf("delete") >0);
          }
          SystemMetadata meta = CNodeService.getInstance(request).getSystemMetadata(session, seriesId);
          assertTrue(meta.getIdentifier().getValue().equals(guid.getValue()));
          
      } catch (Exception e) {
          e.printStackTrace();
          fail(e.getMessage());
      }
      
      
      
      
  }
 
}
