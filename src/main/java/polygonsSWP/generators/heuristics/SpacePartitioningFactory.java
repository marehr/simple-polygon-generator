package polygonsSWP.generators.heuristics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import polygonsSWP.generators.IllegalParameterizationException;
import polygonsSWP.generators.PolygonGenerator;
import polygonsSWP.generators.PolygonGeneratorFactory;
import polygonsSWP.geometry.Point;
import polygonsSWP.geometry.Polygon;
import polygonsSWP.geometry.OrderedListPolygon;
import polygonsSWP.data.PolygonHistory;
import polygonsSWP.data.PolygonStatistics;
import polygonsSWP.util.GeneratorUtils;
import polygonsSWP.util.MathUtils;

public class SpacePartitioningFactory implements PolygonGeneratorFactory {

	@Override
	public boolean acceptsUserSuppliedPoints() {
		return true;
	}

	@Override
	public List<Parameters> getAdditionalParameters() {
		return new LinkedList<Parameters>();
	}

	@Override
	public String toString() {
		return "SpacePartitioning";
	}

	@Override
	public PolygonGenerator createInstance(Map<Parameters, Object> params,
	    PolygonStatistics stats,
			PolygonHistory steps) throws IllegalParameterizationException {
		List<Point> points = GeneratorUtils.createOrUsePoints(params);
		return new SpacePartitioning(points, steps,
				(Integer) params.get(Parameters.size), stats);
	}

	private static class SpacePartitioning implements PolygonGenerator {

		private List<Point> points;
		private PolygonHistory steps = null;
		private boolean doStop = false;
		private int size;
		private PolygonStatistics statistics = null;

		SpacePartitioning(List<Point> points, PolygonHistory steps, int size,
				PolygonStatistics statistics) {
			this.points = points;
			this.steps = steps;
			this.size = size;
			this.statistics = statistics;
		}

		@Override
		public Polygon generate() {
			doStop = false;

			Polygon p = null;
			try {
				p = generate0();
			} catch (RuntimeException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
			}

			return doStop == true ? null : p;
		}

		private Polygon generate0() throws InterruptedException {
			// System.out.println("<------------------------- NEW GENERATE ------------------------->");

			Point first = GeneratorUtils.removeRandomPoint(points), last = GeneratorUtils
					.removeRandomPoint(points);
			// System.out.println("first: " + first + ", last: "+ last);

			List<Point> left = new ArrayList<Point>(points.size()), right = new ArrayList<Point>(
					points.size());

			partionateIn(left, right, points, first, last);

			OrderedListPolygon leftPolygon = spacePartitioning(left, first,
					last), rightPolygon = spacePartitioning(right, last, first);

			// System.out.println("\n\n");
			// System.out.println("result in generate");
			// System.out.println("first: " + first);
			// System.out.println("last: "+ last);
			// System.out.println("left: " + leftPolygon.getPoints());
			// System.out.println("right: " + rightPolygon.getPoints());
			OrderedListPolygon merge = merge(leftPolygon, rightPolygon);
			// System.out.println("polygon: " + merge.getPoints());

			if (!merge.isSimple()) {
				String out = GeneratorUtils.isInGeneralPosition(merge
						.getPoints()) ? "true" : "false";
				throw new RuntimeException("general position: " + out + "\n"
						+ "generated Polygon is not simple: "
						+ merge.getPoints());
			}
			// System.err.println(merge.isSimple()? "simple" : "not simple");

			return merge;
		}

		private void removeDuplicates(Polygon left, Polygon right) {
			List<Point> leftPoints = left.getPoints(), rightPoints = right
					.getPoints();

			assert leftPoints.size() > 0 && rightPoints.size() > 0;

			// on borders can be duplicated elements, we must remove them
			if (rightPoints.get(0)
					.equals(leftPoints.get(leftPoints.size() - 1))) {
				rightPoints.remove(0);
			}

			assert leftPoints.size() > 0 && rightPoints.size() > 0;

			if (leftPoints.get(0).equals(
					rightPoints.get(rightPoints.size() - 1))) {
				leftPoints.remove(0);
			}
		}

		private/* String */void partionateIn(List<Point> left,
				List<Point> right, List<Point> points, Point first, Point last)
				throws InterruptedException {

			if (doStop)
				throw new InterruptedException();

			// String output = "";
			for (Point point : points) {

				int orients = MathUtils.checkOrientation(first, last, point);
				// output += "orients: [" + first + "," + last + "," + point +
				// "]" +
				// (orients < 0 ? "LEFT" : (orients == 0 ? "ONSEGMENT" :
				// "RIGHT")) +
				// "\n";
				if (orients < 0) { // on left-side
					left.add(point);
				} else {
					right.add(point);
				}

			}

			// return /*output*/ null;
		}

		private OrderedListPolygon merge(OrderedListPolygon left,
				OrderedListPolygon right) {
			removeDuplicates(left, right);

			left.getPoints().addAll(right.getPoints());
			return left;
		}

		private OrderedListPolygon spacePartitioning(List<Point> points,
				Point first, Point last) throws InterruptedException {

			if (doStop)
				throw new InterruptedException();

			// base size == 0
			if (points.size() == 0) {
				ArrayList<Point> list = new ArrayList<Point>();
				list.add(first);
				list.add(last);

				// System.out.println("\n\n---size == 0---\npoints: " + points);
				// System.out.println("first: " + first);
				// System.out.println("last: "+ last);
				// System.out.println("draw segment: " + first + " to " + last);
				// System.out.println("------");
				return new OrderedListPolygon(list);
			}

			// base size == 1
			if (points.size() == 1) {
				ArrayList<Point> list = new ArrayList<Point>();
				list.add(first);
				list.add(points.get(0));
				list.add(last);

				// System.out.println("\n\n---size == 1---\npoints: " + points);
				// System.out.println("first: " + first);
				// System.out.println("last: "+ last);
				// System.out.println("draw segment: " + first + " to " +
				// points.get(0)
				// +
				// " to " + last);
				// System.out.println("------");
				return new OrderedListPolygon(list);
			}

			Point middle = GeneratorUtils.removeRandomPoint(points);

			List<Point> left = new ArrayList<Point>(points.size()), right = new ArrayList<Point>(
					points.size());

			// String output =
			partionateIn(left, right, points, first, middle);

			boolean onLeftSide = MathUtils
					.checkOrientation(first, last, middle) == -1;

			OrderedListPolygon leftPolygon = spacePartitioning(
					onLeftSide ? left : right, first, middle), rightPolygon = spacePartitioning(
					onLeftSide ? right : left, middle, last);

			// System.out.println("\n\n---general---\npoints: " + points +
			// ", ordered: "
			// + (onLeftSide ? "LEFT" : "RIGHT"));
			// System.out.println("first: " + first);
			// System.out.println("last: "+ last);
			// System.out.println("middle: " + middle);
			// System.out.println(output);

			// System.out.println("left: " + leftPolygon.getPoints());
			// System.out.println("right: " + rightPolygon.getPoints());

			OrderedListPolygon merge = merge(leftPolygon, rightPolygon);
			// System.out.println("merge: " + merge.getPoints());
			// System.out.println("------");

			return merge;
		}

		@Override
		public void stop() {
			doStop = true;
		}
	}
}
