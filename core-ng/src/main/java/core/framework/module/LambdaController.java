package core.framework.module;

import core.framework.web.Controller;

import java.io.Serializable;

/**
 * @author neo
 */
public interface LambdaController extends Controller, Serializable {
    // due to in JDK, method reference does not have enclosing class/method info,
    // and from JDK 12, ConstantPool can't be access unless run with "--add-opens java.base/jdk.internal.reflect=ALL-UNNAMED"
    // so here use another way to retrieve method reference controller info by serializing
    // refer to ControllerInspector and git history for details
}
