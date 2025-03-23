package dec.core.directory.action;

import dec.core.context.data.ModelData;

public interface DirectoryAction
{

    ActionResult beforeRule(ModelData modelData);

    ActionResult afterRule(ModelData modelData);

}
