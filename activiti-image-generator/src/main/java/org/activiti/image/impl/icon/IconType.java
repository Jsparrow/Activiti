package org.activiti.image.impl.icon;

import org.activiti.image.impl.ProcessDiagramSVGGraphics2D;

public abstract class IconType {

    public abstract Integer getWidth();

    public abstract Integer getHeight();

    public abstract String getAnchorValue();

    public abstract String getFillValue();

    public abstract String getStyleValue();

    public abstract String getDValue();

    public abstract void drawIcon(final int imageX,
                                  final int imageY,
                                  final int iconPadding,
                                  final ProcessDiagramSVGGraphics2D svgGenerator);

    public abstract String getStrokeValue();

    public abstract String getStrokeWidth();
}
