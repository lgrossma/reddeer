package org.jboss.reddeer.gef.handler;

import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jboss.reddeer.gef.api.Palette;
import org.jboss.reddeer.gef.finder.EditPartFinder;
import org.jboss.reddeer.gef.impl.palette.internal.BasicPalette;
import org.jboss.reddeer.junit.logging.Logger;
import org.jboss.reddeer.swt.util.Display;
import org.jboss.reddeer.swt.util.ResultRunnable;

/**
 * Handler for {@link org.eclipse.gef.GraphicalViewer}.
 * 
 * @author Andrej Podhradsky (andrej.podhradsky@gmail.com)
 *
 */
public class ViewerHandler {

	protected final Logger log = Logger.getLogger(this.getClass());

	private static ViewerHandler instance;

	private ViewerHandler() {

	}

	public static ViewerHandler getInstance() {
		if (instance == null) {
			instance = new ViewerHandler();
		}
		return instance;
	}

	/**
	 * Returns palette for a given graphical viewer.
	 * 
	 * @param viewer
	 *            graphical viewer
	 * @return Palette
	 */
	public Palette getPalette(final GraphicalViewer viewer) {
		PaletteViewer paletteViewer = Display.syncExec(new ResultRunnable<PaletteViewer>() {

			@Override
			public PaletteViewer run() {
				return viewer.getEditDomain().getPaletteViewer();
			}
		});
		return new BasicPalette(paletteViewer);
	}

	/**
	 * Returns all editor parts in a given graphical viewer.
	 * 
	 * @param viewer
	 *            Graphical viewer
	 * @return List of edit parts
	 */
	public List<EditPart> getEditParts(final GraphicalViewer viewer) {
		return getEditParts(viewer, new Any());
	}

	/**
	 * Returns all editor parts in a given graphical viewer which fulfill the specified matcher.
	 * 
	 * @param viewer
	 *            Graphical viewer
	 * @param matcher
	 *            Matcher
	 * @return List of edit parts
	 */
	public List<EditPart> getEditParts(final GraphicalViewer viewer, final Matcher<EditPart> matcher) {
		return Display.syncExec(new ResultRunnable<List<EditPart>>() {
			@Override
			public List<EditPart> run() {
				EditPart root = viewer.getContents();
				return new EditPartFinder().find(root, matcher);
			}

		});
	}

	private class Any extends BaseMatcher<EditPart> {

		@Override
		public boolean matches(Object item) {
			return true;
		}

		@Override
		public void describeTo(Description description) {

		}

	}

}