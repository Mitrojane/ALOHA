import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

//Написать моделирующую программу для многоканальной Алоха
public class Main {
	public static void main(String... args) throws IOException {
		int M = 50;
		int numCanal = 2;
		int numWindows = 10000;
		// double Lambda = 0.7;
		double p = (double) 1 / (double) M;
		// mode = 1 -оптимальный ; mode = 0 - 1/M; mode = 2 адаптивный
		for(int  mode = 0; mode < 3; mode++) {

			int i = 0;

			double D[] = new double[20];
			double N[] = new double[20];
			double L[] = new double[20];

			for (double Lambda = 0; Lambda < 1; Lambda += 0.05) {
				double out[] = model(M, numWindows, Lambda, numCanal, p, mode);
				D[i] = out[0];
				N[i] = out[1];
				L[i] = out[2];
				i++;
			}

			System.out.print("D_"+mode+" = [");
			for (int j = 0; j < D.length; j++)
				System.out.print(D[j] + " ");
			System.out.println("];");

			System.out.print("N_"+mode+" = [");
			for (int j = 0; j < N.length; j++)
				System.out.print(N[j] + " ");
			System.out.println("];");

			System.out.print("L_"+mode+" = [");
			for (int j = 0; j < L.length; j++)
				System.out.print(L[j] + " ");
			System.out.println("];\n");
			i = 0;
		}

		System.out.println("m = " + M + ";");
		System.out.println("numCanal = " + numCanal + ";");



	}

	public static double[] model(int M, int numWindows, double Lambda, int numCanal, double p, int mode) {
		LinkedList<LinkedList<Double>> queues = new LinkedList<LinkedList<Double>>();

		for (int i = 0; i < M; i++) {
			queues.add(i, getQueue(M, numWindows, Lambda));
			//System.out.println(queues.get(i).toString());
		}

		// Состояния: 0 - пусто; 1 - успех; 2 - конфликт

		// System.out.println("окно\tстатус\tклиент\tвер");

		int Ncount = 0;
		int Mescount = 0;
		int countConflict = 0;
		double lastP = p;

		LinkedList<Double> D = new LinkedList<Double>();
		LinkedList<Integer> N = new LinkedList<Integer>();

		ArrayList<ArrayList<Integer>> sended = new ArrayList<ArrayList<Integer>>(numCanal);

		for (int i = 0; i < numWindows; i++) {
			for (int r = 0; r < numCanal; r++) {
				sended.add(new ArrayList<Integer>());
				sended.get(r).add(-1);
			}
			for (int j = 0; j < M; j++) {

				if (!queues.get(j).isEmpty()) {
					if (queues.get(j).element() < i) {
						for (int q = 0; q < queues.get(j).size() && queues.get(j).get(q) < i; q++)
							Ncount++;
						if (send(lastP)) {
							int canal = chooseCanal(numCanal);
							sended.get(canal).add(j);
						}
					}
				}
			}

			for (int q = 0; q < numCanal; q++) {
				if (sended.get(q).size() == 2) {
					int h = 0;
					h = h + 10;
					Mescount++;
					double start = queues.get(sended.get(q).get(1)).poll();
					D.add(i + 1 - start);
				}
				if (sended.get(q).size() > 2) {
					countConflict += sended.get(q).size() - 1;
				}
			}
			lastP = getProbability(countConflict, lastP, M, numCanal, p, mode);
			countConflict = 0;
			sended.clear();
			N.add(Ncount);
			Ncount = 0;

		}
		int D_sum = 0;
		for (double el : D) {
			D_sum += el;
		}
		double Daverage = (double) D_sum / (double) D.size();

		int N_sum = 0;
		for (int el : N) {
			N_sum += el;
		}
		double Naverage = (double) N_sum / (double) N.size();

		double LambdaOut = (double) Mescount / (double) numWindows;

		double out[] = new double[3];

		out[0] = Daverage;
		out[1] = Naverage;
		out[2] = LambdaOut;

		/*
		 * System.out.println("lambda: " + Lambda); System.out.println("M[D]: " +
		 * Daverage); System.out.println("M[N]: " + Naverage);
		 * System.out.println("LambdaOut: " + LambdaOut + "\");
		 */

		return out;

	}

	public static LinkedList<Double> getQueue(int M, int numWindows, double Lambda) {
		Random random = new Random();
		LinkedList<Double> tmp = new LinkedList<Double>();
		double sum = 0;
		while (sum < numWindows) {
			double t = ((-1 / (double) (Lambda / M)) * (Math.log(random.nextDouble())));
			tmp.add(t);
			sum += t;
		}
		tmp.removeLast();

		LinkedList<Double> T = new LinkedList<Double>();
		double curTime = 0;
		for (int t = 0; t < tmp.size(); t++) {
			T.add(curTime);
			curTime += tmp.get(t);
		}
		// System.out.println(T);
		return T;
	}

	public static double getProbability(int conflict, double lastP, int M, int NumCanal, double minP, int mode) {
		if (mode == 0)
			return (double)1/M;
		else if (mode == 1)
			return (double)NumCanal/M;
		else {
			double p = 0;
			if (conflict  < M*0.01)
				p = Math.min(1, 2 * lastP);
			else if (conflict  > M*0.1)
				p = Math.max((double) minP, (double) lastP / 2);
			else 
				p = lastP;
			return p;
		}
	}

	public static boolean send(double p) {// true возвращается если отправили
		double a = Math.random();
		if (a >= p)
			return false;
		else
			return true;
	}

	public static int chooseCanal(int amount) {// возвращает номер канала
		int p = (int) (Math.random() * amount);
		return p;
	}

}