package it.unibo.oop.lab.workers02;

import java.util.Arrays;
import java.util.stream.IntStream;


public class MultiThreadedSumMatrix implements SumMatrix {
    private final int nthread;
    private double[][] matrix;

    public MultiThreadedSumMatrix(final int n) {
        this.nthread = n;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int nrows;
        private double res;

        /**
         * Build a new worker.
         * 
         * @param list
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] matrix, final int startpos, final int nrows) {
            super();
            this.matrix = matrix;
            this.startpos = startpos;
            this.nrows = nrows;
        }

        @Override
        public void run() {
            System.out.println("Working from position " + startpos + " to position " + (startpos + nrows - 1));
            for (int i = startpos; i < matrix.length && i < startpos + nrows; i++) {
                this.res += Arrays.stream(matrix[i]).sum();
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         * 
         * @return the sum of every element in the array
         */
        public double getResult() {
            return this.res;
        }

    }

    private static void joinUninterruptibly(final Thread target) {
        var joined = false;
        while (!joined) {
            try {
                target.join();
                joined = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public double sum(final double[][] matrix) {
        final int size = matrix.length % nthread + matrix.length / nthread;
        /*
         * Build a stream of workers
         */
        return IntStream.iterate(0, start -> start + size)
                .limit(nthread)
                .mapToObj(start -> new Worker(matrix, start, size))
                // Start them
                .peek(Thread::start)
                // Join them
                .peek(MultiThreadedSumMatrix::joinUninterruptibly)
                 // Get their result and sum
                .mapToDouble(Worker::getResult)
                .sum();
    }

}
