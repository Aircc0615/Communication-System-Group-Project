package networking;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
	MessageTest.class,
})
public class AllMessageTests{
	
}