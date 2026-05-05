package user;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
	UserTest.class,
	UserLoginModuleTest.class
})
public class AllUserTests{
	
}
