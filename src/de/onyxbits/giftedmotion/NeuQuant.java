package de.onyxbits.giftedmotion;

/* NeuQuant Neural-Net Quantization Algorithm
 * ------------------------------------------
 *
 * Copyright (c) 1994 Anthony Dekker
 *
 * NEUQUANT Neural-Net quantization algorithm by Anthony Dekker, 1994.
 * See "Kohonen neural networks for optimal colour quantization"
 * in "Network: Computation in Neural Systems" Vol. 5 (1994) pp 351-367.
 * for a discussion of the algorithm.
 *
 * Any party obtaining a copy of these files from the author, directly or
 * indirectly, is granted, free of charge, a full and unrestricted irrevocable,
 * world-wide, paid up, royalty-free, nonexclusive right and license to deal
 * in this software and documentation files (the "Software"), including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons who receive
 * copies from any such party to do so, with the only requirement being
 * that this copyright notice remain intact.
 */

// Ported to Java 12/00 K Weiner

public class NeuQuant {

	private static final int netsize = 256; /* number of colours used */

	/* four primes near 500 - assume no image has a length so large */
	/* that it is divisible by all four primes */
	private static final int prime1 = 499;
	private static final int prime2 = 491;
	private static final int prime3 = 487;
	private static final int prime4 = 503;

	private static final int minpicturebytes = (3 * getPrime4());
	/* minimum size for input image */

	/* Program Skeleton
	   ----------------
	   [select samplefac in range 1..30]
	   [read image from input file]
	   pic = (unsigned char*) malloc(3*width*height);
	   initnet(pic,3*width*height,samplefac);
	   learn();
	   unbiasnet();
	   [write output image header, using writecolourmap(f)]
	   inxbuild();
	   write output image using inxsearch(b,g,r)      */

	/* Network Definitions
	   ------------------- */

	private static final int maxnetpos    = (getNetsize() - 1);
	private static final int netbiasshift = 4; /* bias for colour values */
	private static final int ncycles      = 100; /* no. of learning cycles */

	/* defs for freq and bias */
	private static final int intbiasshift = 16; /* bias for fractions */
	private static final int intbias      = (1 << getIntbiasshift());
	private static final int gammashift   = 10; /* gamma = 1024 */
	private static final int gamma        = (1 << getGammashift());
	private static final int betashift    = 10;
	private static final int beta         = (getIntbias() >> getBetashift()); /* beta = 1/1024 */
	private static final int betagamma    = (getIntbias() << (getGammashift() - getBetashift()));

	/* defs for decreasing radius factor */
	private static final int initrad         = (getNetsize() >> 3); /* for 256 cols, radius starts */
	private static final int radiusbiasshift = 6; /* at 32.0 biased by 6 bits */
	private static final int radiusbias      = (1 << getRadiusbiasshift());
	private static final int initradius      = (getInitrad() * getRadiusbias()); /* and decreases by a */
	private static final int radiusdec       = 30; /* factor of 1/30 each cycle */

	/* defs for decreasing alpha factor */
	private static final int alphabiasshift = 10; /* alpha starts at 1.0 */
	private static final int initalpha      = (1 << getAlphabiasshift());
	/* radbias and alpharadbias used for radpower calculation */
	private static final int radbiasshift   = 8;
	private static final int radbias        = (1 << getRadbiasshift());
	private static final int alpharadbshift = (getAlphabiasshift() + getRadbiasshift());
	private static final int alpharadbias   = (1 << getAlpharadbshift());
	private              int alphadec; /* biased by 10 bits */

	/* Types and Global Variables
	-------------------------- */
	private byte[] thepicture; /* the input image itself */
	private int    lengthcount; /* lengthcount = H*W*3 */

	private int samplefac; /* sampling factor 1..30 */

	//   typedef int pixel[4];                /* BGRc */
	private int[][] network; /* the network itself - [netsize][4] */

	private int[] netindex = new int[256];
	/* for network lookup - really 256 */

	private int[] bias     = new int[getNetsize()];
	/* bias and freq arrays for learning */
	private int[] freq     = new int[getNetsize()];
	private int[] radpower = new int[getInitrad()];
	/* radpower for precomputation */

	/* Initialise network in range (0,0,0) to (255,255,255) and set parameters
		 ----------------------------------------------------------------------- */
	public NeuQuant(byte[] thepic,
	                int len,
	                int sample) {

		int   i;
		int[] p;

		setThepicture(thepic);
		setLengthcount(len);
		setSamplefac(sample);

		setNetwork(new int[getNetsize()][]);
		for (i = 0;
		     i < getNetsize();
		     i++) {
			getNetwork()[i] = new int[4];
			p = getNetwork()[i];
			p[0] = p[1] = p[2] = (i << (getNetbiasshift() + 8)) / getNetsize();
			getFreq()[i] = getIntbias() / getNetsize(); /* 1/netsize */
			getBias()[i] = 0;
		}
	}

	public static int getNetsize() {
		return netsize;
	}

	public static int getPrime1() {
		return prime1;
	}

	public static int getPrime2() {
		return prime2;
	}

	public static int getPrime3() {
		return prime3;
	}

	public static int getPrime4() {
		return prime4;
	}

	public static int getMinpicturebytes() {
		return minpicturebytes;
	}

	public static int getMaxnetpos() {
		return maxnetpos;
	}

	public static int getNetbiasshift() {
		return netbiasshift;
	}

	public static int getNcycles() {
		return ncycles;
	}

	public static int getIntbiasshift() {
		return intbiasshift;
	}

	public static int getIntbias() {
		return intbias;
	}

	public static int getGammashift() {
		return gammashift;
	}

	public static int getGamma() {
		return gamma;
	}

	public static int getBetashift() {
		return betashift;
	}

	public static int getBeta() {
		return beta;
	}

	public static int getBetagamma() {
		return betagamma;
	}

	public static int getInitrad() {
		return initrad;
	}

	public static int getRadiusbiasshift() {
		return radiusbiasshift;
	}

	public static int getRadiusbias() {
		return radiusbias;
	}

	public static int getInitradius() {
		return initradius;
	}

	public static int getRadiusdec() {
		return radiusdec;
	}

	public static int getAlphabiasshift() {
		return alphabiasshift;
	}

	public static int getInitalpha() {
		return initalpha;
	}

	public static int getRadbiasshift() {
		return radbiasshift;
	}

	public static int getRadbias() {
		return radbias;
	}

	public static int getAlpharadbshift() {
		return alpharadbshift;
	}

	public static int getAlpharadbias() {
		return alpharadbias;
	}

	public byte[] colorMap() {
		byte[] map   = new byte[3 * getNetsize()];
		int[]  index = new int[getNetsize()];
		for (int i = 0;
		     i < getNetsize();
		     i++) {
			index[getNetwork()[i][3]] = i;
		}
		int k = 0;
		for (int i = 0;
		     i < getNetsize();
		     i++) {
			int j = index[i];
			map[k++] = (byte) (getNetwork()[j][0]);
			map[k++] = (byte) (getNetwork()[j][1]);
			map[k++] = (byte) (getNetwork()[j][2]);
		}
		return map;
	}

	/* Insertion sort of network and building of netindex[0..255] (to do after unbias)
		 ------------------------------------------------------------------------------- */
	public void inxbuild() {

		int   i, j, smallpos, smallval;
		int[] p;
		int[] q;
		int   previouscol, startpos;

		previouscol = 0;
		startpos = 0;
		for (i = 0;
		     i < getNetsize();
		     i++) {
			p = getNetwork()[i];
			smallpos = i;
			smallval = p[1]; /* index on g */
			/* find smallest in i..netsize-1 */
			for (j = i + 1;
			     j < getNetsize();
			     j++) {
				q = getNetwork()[j];
				if (q[1] < smallval) { /* index on g */
					smallpos = j;
					smallval = q[1]; /* index on g */
				}
			}
			q = getNetwork()[smallpos];
			/* swap p (i) and q (smallpos) entries */
			if (i != smallpos) {
				j = q[0];
				q[0] = p[0];
				p[0] = j;
				j = q[1];
				q[1] = p[1];
				p[1] = j;
				j = q[2];
				q[2] = p[2];
				p[2] = j;
				j = q[3];
				q[3] = p[3];
				p[3] = j;
			}
			/* smallval entry is now in position i */
			if (smallval != previouscol) {
				getNetindex()[previouscol] = (startpos + i) >> 1;
				for (j = previouscol + 1;
				     j < smallval;
				     j++) {
					getNetindex()[j] = i;
				}
				previouscol = smallval;
				startpos = i;
			}
		}
		getNetindex()[previouscol] = (startpos + getMaxnetpos()) >> 1;
		for (j = previouscol + 1;
		     j < 256;
		     j++) {
			getNetindex()[j] = getMaxnetpos(); /* really 256 */
		}
	}

	/* Main Learning Loop
		 ------------------ */
	public void learn() {

		int    i, j, b, g, r;
		int    radius, rad, alpha, step, delta, samplepixels;
		byte[] p;
		int    pix, lim;

		if (getLengthcount() < getMinpicturebytes()) {
			setSamplefac(1);
		}
		setAlphadec(30 + ((getSamplefac() - 1) / 3));
		p = getThepicture();
		pix = 0;
		lim = getLengthcount();
		samplepixels = getLengthcount() / (3 * getSamplefac());
		delta = samplepixels / getNcycles();
		alpha = getInitalpha();
		radius = getInitradius();

		rad = radius >> getRadiusbiasshift();
		if (rad <= 1) {
			rad = 0;
		}
		for (i = 0;
		     i < rad;
		     i++) {
			getRadpower()[i] = alpha * (((rad * rad - i * i) * getRadbias()) / (rad * rad));
		}

		//fprintf(stderr,"beginning 1D learning: initial radius=%d\n", rad);

		if (getLengthcount() < getMinpicturebytes()) {
			step = 3;
		} else if ((getLengthcount() % getPrime1()) != 0) {
			step = 3 * getPrime1();
		} else {
			if ((getLengthcount() % getPrime2()) != 0) {
				step = 3 * getPrime2();
			} else {
				if ((getLengthcount() % getPrime3()) != 0) {
					step = 3 * getPrime3();
				} else {
					step = 3 * getPrime4();
				}
			}
		}

		i = 0;
		while (i < samplepixels) {
			b = (p[pix + 0] & 0xff) << getNetbiasshift();
			g = (p[pix + 1] & 0xff) << getNetbiasshift();
			r = (p[pix + 2] & 0xff) << getNetbiasshift();
			j = contest(b,
			            g,
			            r);

			altersingle(alpha,
			            j,
			            b,
			            g,
			            r);
			if (rad != 0) {
				alterneigh(rad,
				           j,
				           b,
				           g,
				           r); /* alter neighbours */
			}

			pix += step;
			if (pix >= lim) {
				pix -= getLengthcount();
			}

			i++;
			if (delta == 0) {
				delta = 1;
			}
			if (i % delta == 0) {
				alpha -= alpha / getAlphadec();
				radius -= radius / getRadiusdec();
				rad = radius >> getRadiusbiasshift();
				if (rad <= 1) {
					rad = 0;
				}
				for (j = 0;
				     j < rad;
				     j++) {
					getRadpower()[j] = alpha * (((rad * rad - j * j) * getRadbias()) / (rad * rad));
				}
			}
		}
		//fprintf(stderr,"finished 1D learning: final alpha=%f !\n",((float)alpha)/initalpha);
	}

	/* Search for BGR values 0..255 (after net is unbiased) and return colour index
		 ---------------------------------------------------------------------------- */
	public int map(int b,
	               int g,
	               int r) {

		int   i, j, dist, a, bestd;
		int[] p;
		int   best;

		bestd = 1000; /* biggest possible dist is 256*3 */
		best = -1;
		i = getNetindex()[g]; /* index on g */
		j = i - 1; /* start at netindex[g] and work outwards */

		while ((i < getNetsize()) || (j >= 0)) {
			if (i < getNetsize()) {
				p = getNetwork()[i];
				dist = p[1] - g; /* inx key */
				if (dist >= bestd) {
					i = getNetsize(); /* stop iter */
				} else {
					i++;
					if (dist < 0) {
						dist = -dist;
					}
					a = p[0] - b;
					if (a < 0) {
						a = -a;
					}
					dist += a;
					if (dist < bestd) {
						a = p[2] - r;
						if (a < 0) {
							a = -a;
						}
						dist += a;
						if (dist < bestd) {
							bestd = dist;
							best = p[3];
						}
					}
				}
			}
			if (j >= 0) {
				p = getNetwork()[j];
				dist = g - p[1]; /* inx key - reverse dif */
				if (dist >= bestd) {
					j = -1; /* stop iter */
				} else {
					j--;
					if (dist < 0) {
						dist = -dist;
					}
					a = p[0] - b;
					if (a < 0) {
						a = -a;
					}
					dist += a;
					if (dist < bestd) {
						a = p[2] - r;
						if (a < 0) {
							a = -a;
						}
						dist += a;
						if (dist < bestd) {
							bestd = dist;
							best = p[3];
						}
					}
				}
			}
		}
		return (best);
	}

	public byte[] process() {
		learn();
		unbiasnet();
		inxbuild();
		return colorMap();
	}

	/* Unbias network to give byte values 0..255 and record position i to prepare for sort
		 ----------------------------------------------------------------------------------- */
	public void unbiasnet() {

		int i, j;

		for (i = 0;
		     i < getNetsize();
		     i++) {
			getNetwork()[i][0] >>= getNetbiasshift();
			getNetwork()[i][1] >>= getNetbiasshift();
			getNetwork()[i][2] >>= getNetbiasshift();
			getNetwork()[i][3] = i; /* record colour no */
		}
	}

	/* Move adjacent neurons by precomputed alpha*(1-((i-j)^2/[r]^2)) in radpower[|i-j|]
		 --------------------------------------------------------------------------------- */
	protected void alterneigh(int rad,
	                          int i,
	                          int b,
	                          int g,
	                          int r) {

		int   j, k, lo, hi, a, m;
		int[] p;

		lo = i - rad;
		if (lo < -1) {
			lo = -1;
		}
		hi = i + rad;
		if (hi > getNetsize()) {
			hi = getNetsize();
		}

		j = i + 1;
		k = i - 1;
		m = 1;
		while ((j < hi) || (k > lo)) {
			a = getRadpower()[m++];
			if (j < hi) {
				p = getNetwork()[j++];
				try {
					p[0] -= (a * (p[0] - b)) / getAlpharadbias();
					p[1] -= (a * (p[1] - g)) / getAlpharadbias();
					p[2] -= (a * (p[2] - r)) / getAlpharadbias();
				} catch (Exception e) {
				} // prevents 1.3 miscompilation
			}
			if (k > lo) {
				p = getNetwork()[k--];
				try {
					p[0] -= (a * (p[0] - b)) / getAlpharadbias();
					p[1] -= (a * (p[1] - g)) / getAlpharadbias();
					p[2] -= (a * (p[2] - r)) / getAlpharadbias();
				} catch (Exception e) {
				}
			}
		}
	}

	/* Move neuron i towards biased (b,g,r) by factor alpha
		 ---------------------------------------------------- */
	protected void altersingle(int alpha,
	                           int i,
	                           int b,
	                           int g,
	                           int r) {

		/* alter hit neuron */
		int[] n = getNetwork()[i];
		n[0] -= (alpha * (n[0] - b)) / getInitalpha();
		n[1] -= (alpha * (n[1] - g)) / getInitalpha();
		n[2] -= (alpha * (n[2] - r)) / getInitalpha();
	}

	/* Search for biased BGR values
		 ---------------------------- */
	protected int contest(int b,
	                      int g,
	                      int r) {

		/* finds closest neuron (min dist) and updates freq */
		/* finds best neuron (min dist-bias) and returns position */
		/* for frequently chosen neurons, freq[i] is high and bias[i] is negative */
		/* bias[i] = gamma*((1/netsize)-freq[i]) */

		int   i, dist, a, biasdist, betafreq;
		int   bestpos, bestbiaspos, bestd, bestbiasd;
		int[] n;

		bestd = ~(1 << 31);
		bestbiasd = bestd;
		bestpos = -1;
		bestbiaspos = bestpos;

		for (i = 0;
		     i < getNetsize();
		     i++) {
			n = getNetwork()[i];
			dist = n[0] - b;
			if (dist < 0) {
				dist = -dist;
			}
			a = n[1] - g;
			if (a < 0) {
				a = -a;
			}
			dist += a;
			a = n[2] - r;
			if (a < 0) {
				a = -a;
			}
			dist += a;
			if (dist < bestd) {
				bestd = dist;
				bestpos = i;
			}
			biasdist = dist - ((getBias()[i]) >> (getIntbiasshift() - getNetbiasshift()));
			if (biasdist < bestbiasd) {
				bestbiasd = biasdist;
				bestbiaspos = i;
			}
			betafreq = (getFreq()[i] >> getBetashift());
			getFreq()[i] -= betafreq;
			getBias()[i] += (betafreq << getGammashift());
		}
		getFreq()[bestpos] += getBeta();
		getBias()[bestpos] -= getBetagamma();
		return (bestbiaspos);
	}

	public int getAlphadec() {
		return alphadec;
	}

	public void setAlphadec(int alphadec) {
		this.alphadec = alphadec;
	}

	public byte[] getThepicture() {
		return thepicture;
	}

	public void setThepicture(byte[] thepicture) {
		this.thepicture = thepicture;
	}

	public int getLengthcount() {
		return lengthcount;
	}

	public void setLengthcount(int lengthcount) {
		this.lengthcount = lengthcount;
	}

	public int getSamplefac() {
		return samplefac;
	}

	public void setSamplefac(int samplefac) {
		this.samplefac = samplefac;
	}

	public int[][] getNetwork() {
		return network;
	}

	public void setNetwork(int[][] network) {
		this.network = network;
	}

	public int[] getNetindex() {
		return netindex;
	}

	public void setNetindex(int[] netindex) {
		this.netindex = netindex;
	}

	public int[] getBias() {
		return bias;
	}

	public void setBias(int[] bias) {
		this.bias = bias;
	}

	public int[] getFreq() {
		return freq;
	}

	public void setFreq(int[] freq) {
		this.freq = freq;
	}

	public int[] getRadpower() {
		return radpower;
	}

	public void setRadpower(int[] radpower) {
		this.radpower = radpower;
	}
}
