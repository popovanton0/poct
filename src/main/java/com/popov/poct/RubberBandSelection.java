/*
 * Copyright 2016 Anton Popov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.popov.poct;

import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by popov on 02.10.2016.
 */
public class RubberBandSelection {

    final DragContext dragContext = new DragContext();
    Rectangle rect = new Rectangle();
    boolean isActive = false;
    ImageView imageView;

    Group group;
    EventHandler<MouseEvent> onMousePressedEventHandler = event -> {

        if (event.isSecondaryButtonDown())
            return;

        // remove old rect
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(0);
        rect.setHeight(0);

        group.getChildren().remove(rect);


        // prepare new drag operation
        dragContext.mouseAnchorX = event.getX();
        dragContext.mouseAnchorY = event.getY();

        rect.setX(dragContext.mouseAnchorX);
        rect.setY(dragContext.mouseAnchorY);
        rect.setWidth(0);
        rect.setHeight(0);

        group.getChildren().add(rect);
    };
    EventHandler<MouseEvent> onMouseDraggedEventHandler = event -> {

        if (event.isSecondaryButtonDown())
            return;

        double offsetX = event.getX() - dragContext.mouseAnchorX;
        double offsetY = event.getY() - dragContext.mouseAnchorY;

        if (offsetX > 0)
            rect.setWidth(offsetX);
        else {
            rect.setX(event.getX());
            rect.setWidth(dragContext.mouseAnchorX - rect.getX());
        }

        if (offsetY > 0) {
            rect.setHeight(offsetY);
        } else {
            rect.setY(event.getY());
            rect.setHeight(dragContext.mouseAnchorY - rect.getY());
        }
    };
    EventHandler<MouseEvent> onMouseReleasedEventHandler = event -> {

        isActive = true;
        if (event.isSecondaryButtonDown())
            return;

        // remove rectangle
        // note: we want to keep the ruuberband selection for the cropping => code is just commented out
            /*
            rect.setX(0);
            rect.setY(0);
            rect.setWidth(0);
            rect.setHeight(0);

            group.getChildren().remove( rect);
            */

    };

    public RubberBandSelection(Group group, ImageView imageView) {

        this.group = group;
        this.imageView = imageView;

        rect = new Rectangle(0, 0, 0, 0);
        rect.setStroke(Color.BLUE);
        rect.setStrokeWidth(1);
        rect.setStrokeLineCap(StrokeLineCap.ROUND);
        rect.setFill(Color.LIGHTGRAY.deriveColor(0, 1.2, 0.6, 0.4));

        group.addEventHandler(MouseEvent.MOUSE_PRESSED, onMousePressedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_DRAGGED, onMouseDraggedEventHandler);
        group.addEventHandler(MouseEvent.MOUSE_RELEASED, onMouseReleasedEventHandler);

    }

    public Bounds getBounds() {
        return rect.getBoundsInParent();
    }

    String crop() {
        Bounds bounds = rect.getBoundsInParent();
        String returnString;

        // remove old rect
        rect.setX(0);
        rect.setY(0);
        rect.setWidth(0);
        rect.setHeight(0);

        group.getChildren().remove(rect);

        int width = (int) bounds.getWidth();
        int height = (int) bounds.getHeight();

        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), width, height));

        WritableImage wi = new WritableImage(width, height);
        imageView.snapshot(parameters, wi);

        // save image
        // !!! has bug because of transparency (use approach below) !!!
        // --------------------------------
//        try {
//          ImageIO.write(SwingFXUtils.fromFXImage( wi, null), "jpg", file);
//      } catch (IOException e) {
//          e.printStackTrace();
//      }


        // save image (without alpha)
        // --------------------------------
        BufferedImage bufImageARGB = SwingFXUtils.fromFXImage(wi, null);
        BufferedImage bufImageRGB = new BufferedImage(bufImageARGB.getWidth(), bufImageARGB.getHeight(), BufferedImage.OPAQUE);

        Graphics2D graphics = bufImageRGB.createGraphics();
        graphics.drawImage(bufImageARGB, 0, 0, null);

        graphics.dispose();

        returnString = CreatorView.importDataFile(bufImageRGB);


        return returnString;
    }

    private static final class DragContext {

        public double mouseAnchorX;
        public double mouseAnchorY;


    }
}
