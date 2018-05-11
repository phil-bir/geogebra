package org.geogebra.common.kernel.advanced;

import org.geogebra.common.kernel.Kernel;
import org.geogebra.common.kernel.arithmetic.Command;
import org.geogebra.common.kernel.commands.CommandProcessor;
import org.geogebra.common.kernel.geos.GeoElement;
import org.geogebra.common.kernel.kernelND.GeoConicND;
import org.geogebra.common.main.MyError;

/**
 * FirstAxisLength[ &lt;GeoConic> ]
 */
public class CmdFirstAxisLength extends CommandProcessor {

	/**
	 * Create new command processor
	 * 
	 * @param kernel
	 *            kernel
	 */
	public CmdFirstAxisLength(Kernel kernel) {
		super(kernel);
	}

	@Override
	final public GeoElement[] process(Command c) throws MyError {
		int n = c.getArgumentNumber();
		GeoElement[] arg;

		switch (n) {
		case 1:
			arg = resArgs(c);

			// asymptotes to conic
			if (arg[0].isGeoConic()) {

				AlgoAxisFirstLength algo = new AlgoAxisFirstLength(cons,
						c.getLabel(), (GeoConicND) arg[0]);

				GeoElement[] ret = { algo.getLength() };
				return ret;
			}
			throw argErr(app, c, arg[0]);

		default:
			throw argNumErr(c);
		}
	}
}
