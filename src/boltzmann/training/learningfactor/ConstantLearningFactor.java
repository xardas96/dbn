package boltzmann.training.learningfactor;

public class ConstantLearningFactor extends AdaptiveLearningFactor {
	private static final long serialVersionUID = -4511656214511806120L;

	public ConstantLearningFactor(double learningFactor) {
		super(learningFactor, 1.0, 1.0);
	}

}